# DayPlan 项目功能实现方案

## 一、RBAC 权限管理（含三员管理）

### 功能描述
- 用户管理：支持用户增删改查
- 角色管理：支持角色菜单权限分配
- 菜单管理：支持配置系统菜单、操作权限、按钮权限标识
- 三员管理：系统管理员、安全审计员、安全保密员三者相互独立、权限分离

### 技术实现方案
- 认证框架：Spring Security 6 + JWT
- 权限模型：RBAC（用户、角色、菜单、关联表）
- 三员管理：预置三个角色 + Security 过滤器拦截敏感操作

### 核心组件位置

```
system/src/main/java/com/xiyao/security/
├── config/
│   └── SecurityConfig.java           # Security 核心配置
├── controller/
│   └── LoginController.java           # 登录控制器
├── details/
│   ├── LoginUser.java                # 登录用户信息
│   └── UserVo.java                    # 用户视图对象
├── enums/
│   └── AdminType.java                 # 管理员类型枚举
├── filter/
│   └── JwtAuthenticationFilter.java   # JWT 认证过滤器
├── handler/
│   ├── AccessDeniedHandlerImpl.java   # 无权限处理
│   └── AuthenticationEntryPointImpl.java # 未登录处理
├── properties/
│   └── SecurityData.java             # 安全配置属性
├── service/
│   └── UserDetailsServiceImpl.java   # 用户详情服务
└── utils/
    ├── JwtUtils.java                  # JWT 工具类
    └── SecurityUtils.java             # 安全工具类
```

### 关键表结构
- sys_user：用户表
- sys_role：角色表
- sys_menu：菜单表
- sys_user_role：用户角色关联表
- sys_role_menu：角色菜单关联表


## 二、国密全链路加解密

### 功能描述
- 接口加解密：请求参数自动解密、响应结果自动加密
- 字段加解密：数据库敏感字段自动加密存储、自动解密读取
- 数据脱敏：返回前端时自动脱敏（手机号、身份证号等）
- 支持算法：SM2（非对称）、SM4（对称）

### 技术实现方案
- 加密库：Bouncy Castle 作为国密算法提供者
- 请求解密：EncryptorFilter + DecryptInterceptor 全局拦截
- 响应加密：EncryptorFilter + EncryptResponseWrapper 处理
- 字段加密：EncryptInterceptor 拦截 SQL 执行
- 字段脱敏：Jackson Serializer + @Sensitive 注解

### 核心组件位置

```
system/src/main/java/com/xiyao/encrypt/
├── annotation/
│   ├── EncryptField.java              # 字段加密注解
│   └── Sensitive.java                 # 字段脱敏注解
├── config/
│   ├── EncryptorApiConfig.java       # API 加解密配置
│   └── EncryptorDataConfig.java       # 数据加解密配置
├── core/
│   ├── EncryptContext.java            # 加密上下文
│   ├── EncryptorManager.java         # 加密管理器
│   ├── IEncryptor.java               # 加密器接口
│   └── encryptor/
│       ├── AbstractEncryptor.java     # 加密器抽象基类
│       ├── Sm2Encryptor.java         # SM2 加密器
│       └── Sm4Encryptor.java          # SM4 加密器
├── enums/
│   ├── AlgorithmType.java            # 算法类型枚举
│   ├── EncodeType.java               # 编码类型枚举
│   └── SensitiveStrategy.java        # 脱敏策略枚举
├── filter/
│   ├── EncryptorFilter.java          # 加解密过滤器
│   └── wrapper/
│       ├── DecryptRequestWrapper.java  # 请求解密包装器
│       └── EncryptResponseWrapper.java # 响应加密包装器
├── interceptor/
│   ├── DecryptInterceptor.java       # 请求解密拦截器
│   └── EncryptInterceptor.java       # 字段加密拦截器
├── properties/
│   ├── EncryptorApi.java             # API 加密属性
│   └── EncryptorData.java            # 数据加密属性
├── serialize/
│   └── SensitiveSerializer.java       # 脱敏序列化器
└── utils/
    └── EncryptUtils.java             # 加密工具类
```

### 加密场景说明
- 登录接口：前端使用 SM2 公钥加密密码传输
- 敏感数据接口：EncryptorFilter 实现全报文加解密
- 数据库字段：使用 @EncryptField 实现字段级别加密
- 审计日志：操作日志中的敏感参数自动脱敏


## 三、日志审计系统（等保合规）

### 功能描述
- 操作日志：记录所有用户的关键操作（增删改查）
- 登录日志：记录登录成功/失败/设备/IP地址/地点
- 审计追踪：支持按条件查询、时间范围筛选
- 事件驱动：基于 Spring Event 异步处理日志

### 技术实现方案
- 日志记录：AOP 切面 + 自定义 @Log 注解
- 异步处理：Spring Event + @Async 异步保存日志，不阻塞业务
- 事件驱动：LogOperationEvent / LogLoginEvent 解耦日志记录
- 监听器：LogListener 处理各类日志事件

### 核心组件位置

```
system/src/main/java/com/xiyao/log/
├── annotation/
│   └── Log.java                       # 日志注解（业务类型、是否保存参数等）
├── aspect/
│   └── LogAspect.java                 # AOP 切面实现日志记录
├── enums/
│   ├── OperationStatus.java          # 操作状态枚举
│   └── OperationType.java            # 操作类型枚举
├── event/
│   ├── LogLoginEvent.java            # 登录日志事件
│   └── LogOperationEvent.java        # 操作日志事件
└── listener/
    └── LogListener.java              # 日志监听器
```

### 日志事件说明
- LogOperationEvent：操作日志事件，携带操作类型、状态等信息
- LogLoginEvent：登录日志事件，携带登录账号、状态、IP等信息
- LogListener：监听器处理事件，可扩展写入数据库或文件系统


## 四、流量治理（限流熔断降级）

### 功能描述
- 限流：基于 QPS 或并发数限制接口访问频率
- 熔断：错误率达到阈值自动熔断服务调用
- 降级：熔断后提供降级响应或兜底数据
- 隔离：限制并发调用，防止资源耗尽
- 重试：失败自动重试指定次数
- 防重提交：基于 Redis + SpEL 防止接口重复提交
- 注解驱动：通过注解声明式配置治理规则

### 技术实现方案
- 治理框架：自定义 AOP 实现（轻量级，适合 Spring Boot 3）
- 限流实现：@RateLimit 注解，基于 RateLimiter 令牌桶
- 熔断实现：@CircuitBreaker 注解，基于 CircuitBreakerManager
- 隔离实现：@Bulkhead 注解，基于信号量隔离
- 重试实现：@Retryable 注解，支持多种重试策略
- 防重实现：@NoRepeatSubmit 注解，基于 Redis + SpEL 表达式
- 配置方式：YAML 配置文件 + @ConfigurationProperties

### 核心组件位置

```
system/src/main/java/com/xiyao/governance/
├── annotation/
│   ├── RateLimit.java                # 限流注解
│   ├── CircuitBreaker.java           # 熔断注解
│   ├── Bulkhead.java                 # 隔离注解
│   ├── Retryable.java                # 重试注解
│   ├── Fallback.java                 # 降级注解
│   └── NoRepeatSubmit.java           # 防重注解
├── config/
│   ├── GovernanceAutoConfig.java     # 治理自动配置
│   └── GovernanceProperties.java      # 治理配置属性
├── controller/
│   └── GovernanceTestController.java # 测试控制器
├── core/
│   ├── bulkhead/
│   │   ├── BulkheadAspect.java       # 隔离切面
│   │   └── BulkheadManager.java      # 隔离管理器
│   ├── circuit/
│   │   ├── CircuitBreakerAspect.java # 熔断切面
│   │   ├── CircuitBreakerManager.java # 熔断管理器
│   │   └── CircuitBreakerState.java  # 熔断状态
│   ├── ratelimit/
│   │   ├── RateLimitAspect.java      # 限流切面
│   │   └── RateLimiter.java         # 限流器
│   ├── retry/
│   │   ├── RetryAspect.java         # 重试切面
│   │   └── RetryContext.java        # 重试上下文
│   └── nosubmit/
│       └── NoRepeatSubmitAspect.java # 防重切面
└── enums/
    ├── CircuitState.java            # 熔断状态枚举
    └── RetryStrategy.java          # 重试策略枚举
```

### 使用示例场景
- 短信发送接口：限流每秒 10 次，防止轰炸
- 外部 API 调用：熔断保护，错误率 50% 触发熔断
- 数据库操作：重试 3 次，处理死锁或超时
- 报表导出：超时 30 秒，长时间任务降级处理
- 表单提交：防重复提交，5秒内相同参数视为重复


## 五、数据字典自动映射

### 功能描述
- 字典维护：前端/后端可维护常用字典（性别、状态、类型等）
- 自动映射：查询结果中 status=0 自动显示为"正常"
- 枚举支持：代码中使用枚举，数据库存 int/string
- 缓存机制：字典数据缓存，减少数据库查询

### 技术实现方案
- 字典表设计：sys_dict_type（字典类型）+ sys_dict_data（字典数据）
- 自动映射：DictResultInterceptor 拦截查询结果
- 枚举处理：DictEnumConverterFactory 处理请求参数转枚举
- 缓存实现：DictCache 本地缓存 + Redis
- 前端组件：封装 el-select 字典组件，传入字典类型自动加载选项

### 核心组件位置

```
system/src/main/java/com/xiyao/dict/
├── annotation/
│   └── DictBind.java                  # 字典绑定注解
├── config/
│   ├── DictAutoConfig.java           # 字典自动配置
│   ├── DictCache.java                # 字典缓存配置
│   ├── DictManager.java              # 字典管理器
│   ├── DictProperties.java           # 字典配置属性
│   └── EnumScanner.java              # 枚举扫描器
├── controller/
│   └── DictTestController.java       # 字典测试控制器
├── converter/
│   ├── DictEnumConverterFactory.java  # 字典枚举转换工厂
│   └── MyEnumConverterFactory.java   # 枚举转换工厂
├── enums/
│   ├── BaseEnum.java                  # 枚举基础接口
│   └── DataStatus.java                # 数据状态枚举
├── interceptor/
│   └── DictResultInterceptor.java     # 结果拦截器
└── service/
    ├── DictService.java              # 字典服务接口
    └── impl/
        └── DictServiceImpl.java      # 字典服务实现
```

### 字典使用示例
- 实体类中直接定义枚举类型字段
- 前端字典组件：`<dict-select dict-type="sys_status" v-model="value" />`
- 接口返回时自动将枚举值转换为显示文本


## 六、链路追踪

### 功能描述
- traceId 生成：基于 UUID 生成全局追踪 ID
- spanId 生成：每次方法调用生成唯一的操作 ID
- 日志集成：通过 MDC 将 traceId 注入日志上下文
- 方法追踪：@Trace 注解标注方法，自动记录调用链路
- 上下文传递：ThreadLocal 存储

### 技术实现方案
- 追踪框架：自定义实现（轻量级，适合高安全等级项目）
- traceId 生成：使用 UUID
- 日志集成：MDC（Mapped Diagnostic Context）存储 traceId
- 方法追踪：AOP 切面 + @Trace 注解
- 上下文传递：TraceContext（ThreadLocal）

### 核心组件位置

```
system/src/main/java/com/xiyao/trace/
├── annotation/
│   └── Trace.java                    # 方法追踪注解
├── aspect/
│   └── TraceAspect.java              # 追踪切面
├── config/
│   ├── TraceAutoConfig.java          # 自动配置
│   └── TraceProperties.java          # 配置属性
├── context/
│   └── TraceContext.java             # 追踪上下文（ThreadLocal）
├── controller/
│   └── TraceTestController.java      # 测试控制器
└── filter/
    └── TraceFilter.java              # Servlet 过滤器，生成 traceId
```

### 使用示例
```java
@Trace(module = "用户管理", operation = "查询用户")
@GetMapping("/{id}")
public Result<User> getUser(@PathVariable Long id) {
    log.info("查询用户信息");
    return Result.success(userService.getById(id));
}
```

### 日志输出示例
```
▶ [用户管理] 查询用户 - traceId=550e8400-e29b-41d4-a716-446655440000, spanId=01
  参数: {"id": 1}
◀ [用户管理] 查询用户 - traceId=550e8400-e29b-41d4-a716-446655440000, spanId=01,耗时=25ms
```