## log 模块

### 模块介绍

```yaml
log 模块提供操作日志和认证日志能力，带链路追踪和 SM3 哈希链防篡改。

核心功能:
  - 操作日志：记录业务增删改查操作，用于问题排查和业务追踪
  - 审计日志：敏感操作记录，仅审计管理员可查，带 SM3 哈希链防篡改
  - 认证日志：记录登录、登出、注册操作，带 SM3 哈希链防篡改
  - 链路追踪：traceId 串联整个请求链路，日志自动携带
  - 异步记录：基于 Spring Event 异步处理，不阻塞业务线程

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 注解驱动：@Log 注解声明式记录，logType 决定日志类型
  - 事件驱动：Spring Event 解耦，与 security 模块无直接依赖
  - SM3 哈希链防篡改：审计日志和认证日志使用国密 SM3 哈希链
  - 三员隔离：审计日志仅审计管理员可查询
```

---

### 技术实现方案

**技术栈：**

```yaml
日志切面: Spring AOP（@Around 环绕通知）
事件机制: Spring Event（ApplicationEventPublisher）
异步处理: @Async + 自定义线程池
链路追踪: Filter + MDC（Mapped Diagnostic Context）
哈希算法: SM3 国密哈希（审计日志、认证日志）
请求信息: MyBaseEvent 自动从 HttpServletRequest 提取
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
        ├── logType = OPERATION → log_operation（无哈希链）
        ├── logType = AUDIT → log_operation（带 SM3 哈希链）
        └── LogLoginEvent → log_login（带 SM3 哈希链）
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
        ├── logType = OPERATION（默认）
        │     │
        │     ▼
        │   操作日志 → log_operation 表
        │     └── 无哈希链，用于业务追踪
        │
        └── logType = AUDIT
              │
              ▼
            审计日志 → log_operation 表
              └── 有 SM3 哈希链，用于等保合规

  认证操作（LOGIN/LOGOUT/REGISTER）
        │
        ▼
  认证日志 → log_login 表
        └── 有 SM3 哈希链，记录登录/登出/注册
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

**SM3 哈希链防篡改机制（审计日志、认证日志）：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                      SM3 哈希链机制                                  │
└─────────────────────────────────────────────────────────────────────┘

  第1条记录：
  hash1 = SM3(seed + user_id + username + ... + prev_hash)
               │
               ▼
  第2条记录：
  hash2 = SM3(hash1 + user_id + username + ... + prev_hash)
               │
               ▼
  第3条记录：
  hash3 = SM3(hash2 + user_id + username + ... + prev_hash)

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
  根据 logType 分流保存
        │
        ├── OPERATION → Db.save()（无哈希链）
        └── AUDIT → Db.save()（带 SM3 哈希链）
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
│   └── LogAutoConfig.java           # 日志模块自动配置
│                                       # - @ConditionalOnProperty 控制开关
│                                       # - FilterRegistrationBean 注册 TraceFilter
│                                       # - @Bean 注册 LogAspect/LogListener
│
├── enums/
│   ├── LogType.java                  # 日志类型
│   │                                   # - OPERATION: 操作日志（默认）
│   │                                   # - AUDIT: 审计日志
│   │
│   ├── OperationStatus.java         # 操作状态
│   │                                   # - SUCCESS = 0
│   │                                   # - FAIL = 1
│   │
│   └── OperationType.java           # 操作类型
│                                       # 0=OTHER 1=QUERY 2=INSERT 3=UPDATE
│                                       # 4=DELETE 5=EXPORT 6=IMPORT
│                                       # 7=LOGIN 8=LOGOUT 9=REGISTER
│
├── event/
│   ├── LogOperationEvent.java        # 操作/审计日志事件
│   │                                   # - 继承 MyBaseEvent
│   │                                   # - userId/username/adminType
│   │                                   # - module/method/type/logType
│   │                                   # - traceId/requestParam/returnResult
│   │                                   # - costTime/status/message
│   │
│   └── LogLoginEvent.java           # 认证日志事件
│                                       # - 继承 MyBaseEvent
│                                       # - userId/username/authType
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

### Bean 注册方式

log 模块采用插件式架构，所有组件通过 `LogAutoConfig` 集中注册：

```java

@Configuration
@ConditionalOnProperty(value = "log.enabled", havingValue = "true", matchIfMissing = true)
public class LogAutoConfig {

    /**
     * 注册链路追踪过滤器
     * <p>
     * 使用 FilterRegistrationBean 注册到 Servlet 容器。
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistrationBean() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 最高优先级
        return registration;
    }

    /**
     * 日志切面
     */
    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    /**
     * 日志监听器
     */
    @Bean
    public LogListener logListener() {
        return new LogListener();
    }
}
```

**注册说明：**

- `@ConditionalOnProperty` 仅加在 `LogAutoConfig` 上
- Filter 使用 `FilterRegistrationBean` 注册到 Servlet 容器
- Aspect 和 Listener 使用普通 `@Bean` 注册

---

### MyBaseEvent 自动获取的信息

LogOperationEvent 和 LogLoginEvent 继承 MyBaseEvent，构造函数自动从 HttpServletRequest 提取：

```java
// 网络信息
clientIp      // 客户端 IP（自动解析 X-Forwarded-For 等）
        clientPort    // 客户端端口
        serverIp      // 服务器地址

// 请求行信息
        requestMethod // HTTP 方法（GET/POST/PUT/DELETE）
        requestUrl    // 请求 URI
        queryString   // 查询参数

// 请求头信息
        userAgent     // 原始 User-Agent
        referer       // 来源页面
        origin        // 跨域来源

// 设备信息（通过 User-Agent 解析）
        os            // 操作系统（如 Windows 10）
        browser       // 浏览器（如 Chrome 100）
        platform      // 平台（PC/Mobile/Tablet）
```

---

### 注解使用

```java
// 操作日志（默认，业务追踪用，无哈希链）
@Log(module = "用户管理", type = OperationType.INSERT)
public Result createUser(@RequestBody User user){...}

// 审计日志（敏感操作，有 SM3 哈希链，仅审计管理员可查）
@Log(module = "权限管理", type = OperationType.DELETE, logType = LogType.AUDIT)
public Result deleteRole(Long id){...}

// 认证操作（自动走 log_login 表）
// LOGIN/LOGOUT/REGISTER 类型自动识别为认证日志
```

**日志类型说明：**

| logType | 存储表 | SM3 哈希链 | 使用场景 |
|---------|--------|-----------|----------|
| OPERATION（默认） | log_operation | 无 | 普通业务操作，问题追踪 |
| AUDIT | log_operation | 有 | 敏感操作，等保审计 |

**操作类型说明：**

| type | 说明 | 建议 |
|------|------|------|
| OTHER | 其他操作 | 默认值 |
| QUERY | 查询操作 | 读操作 |
| INSERT | 新增操作 | 写操作 |
| UPDATE | 更新操作 | 写操作 |
| DELETE | 删除操作 | 写操作 |
| EXPORT | 导出操作 | 数据导出 |
| IMPORT | 导入操作 | 数据导入 |
| LOGIN | 登录 | 认证日志 |
| LOGOUT | 登出 | 认证日志 |
| REGISTER | 注册 | 认证日志 |

---

### 链路追踪使用

```yaml
# 请求时传入 traceId（可选）
Header:
  X-Trace-Id: abc123

# 响应头自动返回 traceId
Header:
  X-Trace-Id: abc123

# 日志中自动携带 traceId
# 通过 MDC 自动注入到日志框架
  2026-05-26 10:30:00.001 INFO  [log-async-1] [abc123] LogListener.saveOperationLog() - 保存操作日志成功
```

**前端排查流程：**

1. 打开浏览器 F12，找到响应的 X-Trace-Id 头
2. 在日志系统中搜索 traceId，快速定位所有相关日志

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
├── base/event/MyBaseEvent.java    # 事件基类（自动获取请求信息）
└── utils/Result.java              # 统一响应

# Security 模块（获取当前用户信息）
src/main/java/com/xiyao/security/
├── utils/SecurityUtils.java       # getUserId/getUsername/getAdminType
├── details/LoginUser.java        # 登录用户详情
└── controller/LoginController.java # 认证接口（发布 LogLoginEvent）

# System 模块（实体）
src/main/java/com/xiyao/system/
├── entity/
│   ├── LogOperation.java       # 操作日志实体（含 hash/prev_hash 字段）
│   └── LogLogin.java          # 认证日志实体
```

---

### 关键表结构

```sql
-- 操作日志表（业务追踪/审计用）
-- OPERATION 类型无哈希链，AUDIT 类型带 SM3 哈希链
CREATE TABLE log_operation
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT COMMENT '操作用户ID',
    username         VARCHAR(50) COMMENT '操作用户账号',
    admin_type       INT COMMENT '三员类型 0-普通用户 1-系统管理员 2-安全管理员 3-审计管理员',
    operation_module VARCHAR(100) COMMENT '操作模块',
    operation_method VARCHAR(200) COMMENT '操作方法',
    operation_type   INT COMMENT '操作类型',
    request_method   VARCHAR(10) COMMENT '请求方式',
    request_url      VARCHAR(200) COMMENT '请求URL',
    request_param    TEXT COMMENT '请求参数',
    return_result    TEXT COMMENT '返回结果',
    status           INT    DEFAULT 0 COMMENT '状态 0-失败 1-成功',
    message          VARCHAR(500) COMMENT '提示消息',
    cost_time        BIGINT DEFAULT 0 COMMENT '消耗时间（毫秒）',
    ip               VARCHAR(50) COMMENT '客户端IP',
    os               VARCHAR(100) COMMENT '操作系统',
    browser          VARCHAR(100) COMMENT '浏览器',
    platform         VARCHAR(50) COMMENT '平台类型',
    trace_id         VARCHAR(32) COMMENT '链路追踪ID',
    operation_time   DATETIME COMMENT '操作时间',
    hash             VARCHAR(64) COMMENT '本记录SM3哈希值（AUDIT类型有）',
    prev_hash        VARCHAR(64) COMMENT '上一条记录哈希值（AUDIT类型有）'
) COMMENT '操作日志';

-- 认证日志表（登录/登出/注册，带 SM3 哈希链）
CREATE TABLE log_login
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT COMMENT '用户ID',
    username   VARCHAR(50) COMMENT '账号',
    auth_type  INT COMMENT '认证类型 7-登录 8-登出 9-注册',
    status     INT DEFAULT 0 COMMENT '状态 0-失败 1-成功',
    message    VARCHAR(255) COMMENT '提示消息',
    ip         VARCHAR(50) COMMENT '客户端IP',
    os         VARCHAR(100) COMMENT '操作系统',
    browser    VARCHAR(100) COMMENT '浏览器',
    platform   VARCHAR(50) COMMENT '平台类型',
    trace_id   VARCHAR(32) COMMENT '链路追踪ID',
    login_time DATETIME COMMENT '认证时间',
    hash       VARCHAR(64) NOT NULL COMMENT '本记录SM3哈希值',
    prev_hash  VARCHAR(64) COMMENT '上一条记录哈希值'
) COMMENT '认证日志';
```

---

### 配置文件

```yaml
# application.yml
# log 模块配置（插件式，通过 @ConditionalOnProperty 控制）
log:
  enabled: true  # 默认 true，可不配置
```

---

### 安全合规说明

```java
// 等保合规要求
// 1. 操作日志禁止物理删除，仅能逻辑删除
// 2. 审计日志（AUDIT 类型）禁止修改，仅审计管理员可查询
// 3. 认证日志（登录/登出/注册）必须记录 IP 和设备信息
// 4. SM3 哈希链机制确保审计日志和认证日志完整性

// 三员权限
// - 操作日志（OPERATION）：系统管理员、安全管理员
// - 审计日志（AUDIT）：仅审计管理员
// - 认证日志：系统管理员、安全管理员、审计管理员
```