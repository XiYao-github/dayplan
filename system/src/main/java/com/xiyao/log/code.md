## log 模块

### 模块介绍

```yaml
log 模块提供操作日志和认证日志能力，带链路追踪和 SM3 哈希链防篡改。

核心功能:
  - 操作日志：记录业务增删改查操作，用于问题排查和业务追踪
  - 认证日志：记录登录、登出、注册操作，带 SM3 哈希链防篡改
  - 链路追踪：traceId 串联整个请求链路，日志自动携带
  - 异步记录：基于 Spring Event 异步处理，不阻塞业务线程

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 注解驱动：@Log 注解声明式记录
  - 事件驱动：Spring Event 解耦，与 security 模块无直接依赖
  - SM3 哈希链防篡改：认证日志使用国密 SM3 哈希链
```

---

### 技术实现方案

**技术栈：**

```yaml
日志切面: Spring AOP（@Around 环绕通知）
事件机制: Spring Event（ApplicationEventPublisher）
异步处理: @Async + 自定义线程池
链路追踪: Filter + MDC（Mapped Diagnostic Context）
哈希算法: SM3 国密哈希（认证日志）
请求信息: MyBaseEvent 自动从 HttpServletRequest 提取信息
```

**模块结构：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          log 模块结构                                │
└─────────────────────────────────────────────────────────────────────┘

  LogAutoConfig（模块自动配置）
        │
        ├── @ConditionalOnProperty 控制开关
        ├── FilterRegistrationBean 注册 TraceFilter
        ├── @Bean 注册 LogAspect
        └── @Bean 注册 LogListener

  TraceFilter（链路追踪过滤器）
        │
        ├── 生成/获取 traceId
        ├── 放入 MDC
        └── 响应头返回 X-Trace-Id

  LogAspect（日志切面）
        │
        ├── @Around("@annotation(log)")
        ├── 从 MDC 获取 traceId
        ├── 继承 MyBaseEvent 自动获取请求信息
        └── 发布 LogOperationEvent

  LogListener（事件监听）
        │
        ├── @EventListener + @Async
        ├── LogOperationEvent → log_operation
        └── LogLoginEvent → log_login（SM3 哈希链）
```

**日志类型分流：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        日志类型分流                                   │
└─────────────────────────────────────────────────────────────────────┘

  @Log 注解标记方法
        │
        ▼
  LogAspect 拦截
        │
        ▼
  操作日志 → log_operation 表（无哈希链）

  认证操作（LOGIN/LOGOUT/REGISTER）
        │
        ▼
  认证日志 → log_login 表（带 SM3 哈希链）
```

**链路追踪流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        链路追踪流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  请求进入（带 X-Trace-Id？）
        │
        ▼
  TraceFilter（log.filter.TraceFilter）
        │
        ├── 检查 X-Trace-Id 请求头
        │     ├── 前端传入：使用传入的 traceId
        │     └── 前端未传：生成 UUID
        │
        ▼
  MDC.put("traceId", "xxx")
        │
        ▼
  响应头返回 X-Trace-Id: xxx
        │
        ▼
  Controller / Service 业务处理
        │
        ▼
  @Log 触发 LogAspect
        │
        ▼
  MDC.get("traceId") 获取
        │
        ▼
  LogOperationEvent.traceId = xxx
        │
        ▼
  异步保存到数据库，日志记录 traceId
        │
        ▼
  finally: MDC.remove("traceId")
```

**SM3 哈希链防篡改机制（认证日志）：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                      SM3 哈希链机制                                  │
└─────────────────────────────────────────────────────────────────────┘

  第1条记录：
  hash1 = SM3(id + "|" + prev_hash)
               │
               ▼
  第2条记录：
  hash2 = SM3(id + "|" + prev_hash)
               │
               ▼
  验证方式：
  1. 重新计算每条记录的 SM3 哈希值
  2. 对比存储的 hash 值
  3. 任意记录被修改，后续所有 hash 不匹配
```

**事件发布订阅流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        事件发布订阅流程                               │
└─────────────────────────────────────────────────────────────────────┘

  @Log 标注的方法执行
        │
        ▼
  LogAspect.around()
        │
        ├── 执行前：构建 LogOperationEvent
        │     ├── 继承 MyBaseEvent → 自动获取请求信息
        │     ├── MDC 获取 traceId
        │     └── SecurityUtils 获取用户信息
        │
        ├── 执行方法
        │
        ├── 执行后：设置结果/异常
        │
        └── finally：SpringUtil.publishEvent() 发布事件
        │
        ▼
  Spring Event 机制
        │
        ▼
  LogListener 监听
        │
        ├── @EventListener(LogOperationEvent.class)
        ├── @Async("logExecutor") 异步执行
        │
        ▼
  Db.save() → log_operation
```

---

### 组件结构

```tree
com.xiyao.log/
├── annotation/
│   └── Log.java                       # 日志注解
│                                       # - module: 操作模块
│                                       # - type: 操作类型（OperationType）
│                                       # - logType: 日志类型（LogType）
│                                       # - isSaveRequestData: 保存请求参数
│                                       # - isSaveResponseData: 保存响应数据
│
├── aspect/
│   └── LogAspect.java                 # AOP 切面（由 LogAutoConfig 注册）
│                                       # - @Around("@annotation(log)")
│                                       # - 构建事件（继承 MyBaseEvent）
│                                       # - 从 MDC 获取 traceId
│                                       # - SpringUtil.publishEvent()
│
├── config/
│   └── LogAutoConfig.java            # 日志模块自动配置
│                                       # - @ConditionalOnProperty 控制开关
│                                       # - FilterRegistrationBean 注册 TraceFilter
│                                       # - @Bean 注册 LogAspect/LogListener
│
├── enums/
│   ├── LogType.java                  # 日志类型
│   │                                   # - OPERATION: 操作日志
│   │
│   ├── OperationStatus.java          # 操作状态
│   │                                   # - SUCCESS = 0
│   │                                   # - FAIL = 1
│   │
│   └── OperationType.java            # 操作类型
│                                       # 0=OTHER 1=QUERY 2=INSERT 3=UPDATE
│                                       # 4=DELETE 5=EXPORT 6=IMPORT
│                                       # 7=LOGIN 8=LOGOUT 9=REGISTER
│
├── event/
│   ├── LogOperationEvent.java        # 操作日志事件
│   │                                   # - 继承 MyBaseEvent
│   │                                   # - userId/username
│   │                                   # - module: 业务模块（如"用户管理"）
│   │                                   # - path: 操作路径（全类名.方法名）
│   │                                   # - type/status/message/param/result/cost
│   │
│   └── LogLoginEvent.java            # 认证日志事件
│                                       # - 继承 MyBaseEvent
│                                       # - userId/username/type
│                                       # - status/message/traceId
│
├── filter/
│   └── TraceFilter.java              # 链路追踪过滤器
│                                       # - 检查/生成 traceId
│                                       # - MDC.put/remove
│                                       # - 响应头返回 X-Trace-Id
│                                       # - 由 LogAutoConfig 通过 FilterRegistrationBean 注册
│
└── listener/
    └── LogListener.java              # 日志监听器（由 LogAutoConfig 注册）
                                        # - @Async("logExecutor")
                                        # - saveOperationLog() → log_operation
                                        # - saveLoginLog() → log_login（SM3 哈希链）
```

---

### API 接口清单

日志模块通过 `@Log` 注解自动记录日志，无需手动调用。以下是日志查询接口：

```yaml
# System 模块（日志查询接口）
GET    /system/log-login/list         # 认证日志列表
GET    /system/log-operation/list     # 操作日志列表

# 日志查询参数
pageNum: int      # 页码，默认1
pageSize: int     # 每页条数，默认10
username: string  # 用户账号
startTime: string  # 开始时间
endTime: string   # 结束时间

# 操作日志额外参数
module: string   # 操作模块
status: int      # 操作状态
```

---

### Bean 注册方式

log 模块采用插件式架构，所有组件通过 `LogAutoConfig` 集中注册：
- `@ConditionalOnProperty` 仅加在 `LogAutoConfig` 上
- Filter 使用 `FilterRegistrationBean` 注册到 Servlet 容器
- Aspect 和 Listener 使用普通 `@Bean` 注册

---

### MyBaseEvent 自动获取的信息

LogOperationEvent 和 LogLoginEvent 继承 MyBaseEvent，构造函数自动从 HttpServletRequest 提取：

```java
// 网络信息
// clientIp      // 客户端 IP（自动解析 X-Forwarded-For 等）
// clientPort    // 客户端端口
// serverIp      // 服务器地址

// 请求行信息
// requestMethod // HTTP 方法（GET/POST/PUT/DELETE）
// requestUrl    // 请求 URI
// queryString   // 查询参数

// 请求头信息
// userAgent     // 原始 User-Agent
// referer      // 来源页面
// origin       // 跨域来源

// 设备信息（通过 User-Agent 解析）
// os            // 操作系统（如 Windows 10）
// browser       // 浏览器（如 Chrome 100）
// platform      // 平台（PC/Mobile/Tablet）
```

---

### 注解使用

```java
// 操作日志
// @Log(module = "用户管理", type = OperationType.INSERT)
// public Result createUser(@RequestBody User user){...}

// 认证操作（自动走 log_login 表）
// LOGIN/LOGOUT/REGISTER 类型自动识别为认证日志
```

**module 与 path 的区别：**

```java
// @Log(module = "用户管理", type = OperationType.INSERT)
// 记录到 log_operation 表：
//   module = "用户管理"（业务分类，便于按模块统计）
//   path = "com.xiyao.system.controller.UserController.createUser"（具体方法位置）
```

**日志类型说明：**

```java
// LogType{
//     OPERATION  // 操作日志
// }
```

**操作类型说明：**

```java
// OperationType{
//     OTHER,      // 其他操作（默认值）
//     QUERY,      // 查询操作（读操作）
//     INSERT,     // 新增操作（写操作）
//     UPDATE,     // 更新操作（写操作）
//     DELETE,     // 删除操作（写操作）
//     EXPORT,     // 导出操作（数据导出）
//     IMPORT,     // 导入操作（数据导入）
//     LOGIN,      // 登录（认证日志）
//     LOGOUT,     // 登出（认证日志）
//     REGISTER    // 注册（认证日志）
// }
```

---

### 链路追踪使用

```yaml
# 请求时传入 traceId（可选）
# Header: X-Trace-Id: abc123

# 响应头自动返回 traceId
# Header: X-Trace-Id: abc123

# 日志中自动携带 traceId
# 通过 MDC 自动注入到日志框架
# 2026-06-09 10:30:00.001 INFO  [log-async-1] [abc123] LogListener.saveOperationLog() - 保存操作日志成功
```

**前端排查流程：**

1. 打开浏览器 F12，找到响应的 X-Trace-Id 头
2. 在日志系统中搜索 traceId，快速定位所有相关日志

---

### 依赖文件路径

```tree
# Common 基础类（被其他模块继承）
src/main/java/com/xiyao/common/
├── base/
│   ├── controller/MyBaseController.java    # 控制器基类
│   ├── entity/MyBaseEntity.java          # 实体基类（公共字段）
│   ├── event/MyBaseEvent.java            # 事件基类（自动获取请求信息）
│   ├── mapper/MyBaseMapper.java           # Mapper 基类
│   ├── service/MyBaseService.java         # Service 接口基类
│   └── service/impl/MyBaseServiceImpl.java # Service 实现基类
├── constant/Constant.java               # 通用常量
└── utils/
    ├── Result.java                       # 统一响应封装
    ├── RedisUtils.java                   # Redis 工具类
    ├── SpringUtils.java                  # Spring 工具类
    └── WebUtils.java                     # Web 工具类

# Security 模块（获取当前用户信息）
src/main/java/com/xiyao/security/
├── config/SecurityConfig.java            # Spring Security 配置
├── controller/LoginController.java       # 认证控制器（发布 LogLoginEvent）
├── details/LoginUser.java               # 登录用户详情
├── details/UserVo.java                  # 用户视图对象
├── filter/JwtAuthenticationFilter.java   # JWT 认证过滤器
├── handler/AccessDeniedHandlerImpl.java # 权限不足处理器
├── handler/AuthenticationEntryPointImpl.java # 认证失败处理器
├── properties/SecurityData.java        # Security 配置属性
├── service/SecurityService.java         # 安全服务
├── utils/JwtUtils.java                    # JWT 工具类
└── utils/SecurityUtils.java                # 安全工具类

# System 模块（实体）
src/main/java/com/xiyao/system/
├── entity/
│   ├── LogOperation.java       # 操作日志实体（含 hash/prev_hash 字段）
│   └── LogLogin.java          # 认证日志实体
└── mapper/
    ├── LogOperationMapper.java
    └── LogLoginMapper.java
```

---

### 配置文件

```yaml
# application.yml
# log 模块配置（插件式，通过 @ConditionalOnProperty 控制）
log-data:
  enable: true  # 默认 true，可不配置
```

---

### 安全合规说明

```java
// 等保合规要求
// 1. 操作日志禁止物理删除，仅能逻辑删除
// 2. 认证日志（登录/登出/注册）必须记录 IP 和设备信息
// 3. SM3 哈希链机制确保认证日志完整性
```
