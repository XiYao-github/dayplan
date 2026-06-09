## framework 模块

### 模块介绍

```yaml
framework 模块是 DayPlan 框架的基础配置模块，为所有模块提供底层支撑能力。

核心功能:
  - Web 配置：Jackson、跨域、拦截器、参数解析器
  - 数据访问：MyBatis-Plus 分页、乐观锁、自动填充、逻辑删除
  - 缓存支持：Redis 序列化、Spring Cache 缓存管理
  - 异步处理：自定义线程池、日志异步执行
  - 异常处理：统一异常捕获与响应封装
  - 参数注入：@CurrentUser 注解自动注入当前登录用户

技术特点:
  - 自动配置：通过 @Configuration 和 @Bean 声明式配置
  - 插件式设计：各配置类独立，可按需启用
  - 全局统一：ObjectMapper、线程池等全局组件统一管理
```

---

### 技术实现方案

**技术栈：**

```yaml
Web 配置: WebMvcConfigurer
JSON 处理: Jackson + JavaTimeModule
数据库: MyBatis-Plus + HikariCP
缓存: Redis + Spring Cache
异步: @Async + ThreadPoolTaskExecutor
异常处理: @RestControllerAdvice
参数解析: HandlerMethodArgumentResolver
```

**请求处理流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          请求处理流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  HTTP 请求进入
        │
        ▼
  Filter 过滤器链（Security、JWT、TraceId 等）
        │
        ▼
  DispatcherServlet 分发
        │
        ▼
  HandlerMapping 查找 Controller
        │
        ▼
  HandlerAdapter 执行（参数解析 → 执行方法）
        │
        ▼
  ArgumentResolver（@CurrentUser 等自定义解析）
        │
        ▼
  Controller 方法执行
        │
        ▼
  异常统一被 @RestControllerAdvice 捕获
        │
        ▼
  返回 Result 统一响应
```

**异常处理流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          异常处理流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  业务方法抛出异常
        │
        ▼
  GlobalExceptionHandler 统一捕获
        │
        ├── ValidationException → 参数校验错误
        ├── AuthenticationException → 认证失败（401）
        ├── AccessDeniedException → 权限不足（403）
        ├── DuplicateKeyException → 数据重复（409）
        ├── BusinessException → 业务异常（自定义码）
        └── 其他 Exception → 系统异常（500）
        │
        ▼
  返回统一 Result 响应
```

**自动填充流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        自动填充流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  插入/更新操作
        │
        ▼
  MyBatis-Plus 拦截
        │
        ▼
  MetaObjectHandler.insertFill/updateFill
        │
        ▼
  SecurityUtils.getUserId() 获取当前用户
        │
        ▼
  填充 createBy/createTime/updateBy/updateTime
```

---

### 组件结构

```tree
com.xiyao.framework/
├── annotation/
│   └── CurrentUser.java               # 当前登录用户注解
│                                       # 标注在 Controller 参数上自动注入
│
├── config/
│   ├── JacksonConfig.java            # Jackson 配置
│   │                                   # - 日期时间格式：yyyy-MM-dd HH:mm:ss
│   │                                   # - Long 转字符串防精度丢失
│   │                                   # - 空值不序列化
│   │
│   ├── MybatisPlusConfig.java        # MyBatis-Plus 配置
│   │                                   # - 分页插件（DbType.MYSQL）
│   │                                   # - 乐观锁插件（@Version）
│   │                                   # - 防全表操作插件
│   │                                   # - 自动填充（createBy/time、updateBy/time）
│   │
│   ├── RedisConfig.java              # Redis 配置
│   │                                   # - RedisTemplate（String-JSON）
│   │                                   # - CacheManager（Spring Cache）
│   │                                   # - 序列化方式配置
│   │
│   ├── ThreadPoolConfig.java         # 线程池配置
│   │                                   # - logExecutor：日志异步线程池
│   │                                   # - corePoolSize=2, maxPoolSize=5
│   │
│   └── WebMvcConfig.java            # Web MVC 配置
│                                       # - CORS 跨域配置
│                                       # - 静态资源处理
│                                       # - 拦截器配置
│                                       # - 参数解析器（@CurrentUser）
│                                       # - 消息转换器
│
├── exception/
│   ├── BusinessException.java        # 业务异常
│   │                                   # - code: HTTP 状态码
│   │                                   # - message: 错误信息
│   │
│   └── GlobalExceptionHandler.java    # 全局异常处理器
│                                       # - 参数校验异常（400）
│                                       # - 认证/权限异常（401/403）
│                                       # - 数据库异常（DuplicateKey 等）
│                                       # - 业务异常（BusinessException）
│                                       # - 系统异常（500）
│
│
├── resolver/
│   └── CurrentUserArgumentResolver.java # @CurrentUser 参数解析器
│
└── utils/
    ├── RedisUtils.java               # Redis 工具类
    ├── SpringUtils.java              # Spring 容器工具类
    └── WebUtils.java                  # Web 工具类
```

---

### 关键配置说明

**JacksonConfig：**

```java
// 日期时间格式
// DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
// DATE_PATTERN = "yyyy-MM-dd"
// TIME_PATTERN = "HH:mm:ss"
// TIME_ZONE = "GMT+8"

// 数值类型序列化转字符串
// 解决 Long 类型在 JavaScript 中精度丢失
// Long → ToStringSerializer
// BigInteger → ToStringSerializer
// BigDecimal → ToStringSerializer
```

**MybatisPlusConfig：**

```java
// 分页配置
// DbType.MYSQL
// MaxLimit = 1000

// 防全表操作
// BlockAttackInnerInterceptor（禁止无 WHERE 的 UPDATE/DELETE）

// 乐观锁
// @Version 注解标注版本号字段

// 自动填充字段
// insert: createBy, createTime, updateBy, updateTime
// update: updateBy, updateTime

// 逻辑删除
// deleted = 1 表示已删除
```

**RedisConfig：**

```java
// Key 序列化：StringRedisSerializer
// Value 序列化：Jackson2JsonRedisSerializer（带 @class 类型信息）
// 默认过期时间：1小时
```

**ThreadPoolConfig：**

```java
// 日志异步线程池
// corePoolSize = 2
// maxPoolSize = 5
// queueCapacity = 100
// threadNamePrefix = "log-async-"
// rejectedHandler = CallerRunsPolicy
```

---

### 依赖文件路径

```tree
# Common 基础类（被其他模块继承）
src/main/java/com/xiyao/common/
├── base/
│   ├── controller/MyBaseController.java    # Controller 基类
│   ├── entity/MyBaseEntity.java          # 实体基类（公共字段）
│   ├── event/MyBaseEvent.java            # 事件基类（自动获取请求信息）
│   ├── mapper/MyBaseMapper.java           # Mapper 基类
│   ├── service/MyBaseService.java         # Service 接口基类
│   └── service/impl/MyBaseServiceImpl.java # Service 实现基类
├── constant/Constant.java               # 通用常量
├── enums/Status.java                     # 状态枚举
└── utils/
    ├── CodeGenerator.java               # 代码生成器
    ├── ExcelUtils.java                   # Excel 工具
    ├── FileUtils.java                    # 文件工具
    ├── StreamUtils.java                   # 流式处理工具
    └── data/
        ├── PageResult.java               # 分页结果封装
        └── Result.java                   # 统一响应封装

# Security 模块（自动填充依赖）
src/main/java/com/xiyao/security/
├── config/SecurityConfig.java            # Spring Security 配置
├── controller/LoginController.java       # 认证控制器（登录/注册/登出）
├── details/LoginUser.java               # 登录用户详情
├── details/UserVo.java                  # 用户视图对象
├── filter/JwtAuthenticationFilter.java   # JWT 认证过滤器
├── handler/AccessDeniedHandlerImpl.java # 权限不足处理器
├── handler/AuthenticationEntryPointImpl.java # 认证失败处理器
├── properties/SecurityData.java        # Security 配置属性
├── service/SecurityService.java         # 安全服务
├── service/impl/UserDetailsServiceImpl.java # 用户信息加载服务
├── utils/JwtUtils.java                   # JWT 工具类
└── utils/SecurityUtils.java               # 安全工具类

# Dict 模块（枚举和字典）
src/main/java/com/xiyao/dict/
├── annotation/DictBind.java             # 字典绑定注解
├── config/DictAutoConfig.java          # 字典模块自动配置
├── enums/
│   ├── BaseEnum.java                    # 枚举基础接口
│   └── DataStatus.java                  # 数据状态枚举
├── interceptor/DictInterceptor.java   # 字典结果拦截器
└── utils/DictUtils.java                # 字典缓存工具
```
