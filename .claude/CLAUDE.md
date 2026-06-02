# Dayplan 后台框架

## 项目概述

项目名称：Dayplan（公安网高安全等级企业级全栈框架）
架构模式：Spring Boot 3 单体式多模块 Maven 项目，前后端分离
安全等级：等保合规，公安网标准
核心理念：以可插拔模块化方式组织代码，通过配置开关控制功能加载，保持核心稳定，结构解耦，易于扩展
基础包名：com.xiyao

---

## 模块结构与职责

- 项目采用分层、分模块的设计，分为三层：
- system（基础能力层）
- service（业务逻辑层）
- app（对外接口层）
- 当前框架状态下 service 与 app 层为空，所有通用能力均由 system 层提供。

### system 层（基础能力层）

基础包名：com.xiyao.system

承载所有通用能力，与具体业务无关，可在任何项目中复用。当前包含以下子模块：

- framework：基于 Spring Boot 3 的自动配置模块，封装 WebMvc、Jackson、MyBatis-Plus、Redis 等基础依赖配置，是所有模块的底层支撑
- security：集成 Spring Security 6 + JWT，实现细粒度 RBAC 权限控制，并扩展支持等保要求的三员管理
- crypto：国密算法支持模块，全面适配等保密码要求
- dict：数据字典回显、自动映射枚举
- log：满足等保合规的审计日志模块，支持操作日志防篡改（签名哈希链）和全链路追踪（traceId）
- common：通用工具集
  - 包含动态线程池管理
  - 文件存储（本地存储、云存储、日志下载）
  - 短信邮件发送
  - EasyExcel 工具封装
  - Smart-Doc 接口文档生成配置
  - 代码生成器
  - 测试数据生成
- system：区别于 service 和 app ，这里主要提供给 system 层使用的系统级模块
  - controller：系统级接口
  - entity：数据库实体类，对应数据库表结构
  - mapper：MyBatis-Plus Mapper 接口
  - service：业务逻辑实现类
  - vo：返回给前端的视图对象，禁止直接返回 entity

### service 层（业务逻辑层）

基础包名：com.xiyao.service
纯业务逻辑实现，按领域划分模块，依赖 system 层提供的各种能力。
目录结构规范：每个业务领域模块下按照 entity、mapper、service、vo 划分子包。
- entity：数据库实体类，对应数据库表结构
- mapper：MyBatis-Plus Mapper 接口
- service：业务逻辑实现类
- vo：返回给前端的视图对象，禁止直接返回 entity
  该层不直接暴露 HTTP 接口，通过 app 层被调用，保证逻辑与外部协议解耦。
  当前框架状态下为空，业务开发时在此添加模块。

### app 层（对外接口层）

基础包名：com.xiyao.app
将 service 层的业务能力以不同接口形式暴露给外部消费者。按消费端划分子包：
- admin：管理后台接口，供 Vue3 管理端消费
- app：小程序接口，供 UniApp 小程序端消费
- api：开放 API 接口，供第三方系统对接，采用 SM3 签名保障安全

Controller 只能放在 app 层，不能在 service 层或 system 层定义 Controller。
Controller 的职责仅限于：接收请求参数、调用 Service 方法、包装 Result 响应。复杂业务逻辑必须下沉到 service 层。
当前框架状态下为空，业务开发时在此添加模块。

---

## 三员管理详解

三员相互制约，账户严格分离，任何一人无法完成超越职责的操作。
系统管理员：负责系统配置、用户账号管理。可以创建用户并指定其为安全保密管理员，但不能操作业务数据，不能查看安全审计日志。
安全保密管理员：负责用户权限分配、安全策略设置。可以分配菜单权限、设置密码策略、配置会话超时时间。不能查看审计日志，无法知道自己被审计管理员监督的情况。
安全审计管理员：负责查看和导出审计日志，监督其他两员操作。拥有日志完整查看权限，但没有任何系统配置权限和权限分配权限。
三员账户类型在创建时标记，创建后不可更改类型。权限交集被严格禁止，代码层面必须防止某角色被赋予跨职责权限。

---

## crypto 模块详解

### 算法支持

集成 SM2、SM3、SM4 国密算法，底层使用 Hutool + BouncyCastle 实现。

- SM2：非对称加密算法，用于密钥交换和数字签名
- SM3：哈希算法，用于数据完整性校验
- SM4：对称加密算法，用于数据批量加解密

### 数据库透明加解密

通过 MyBatis 拦截器实现，对业务代码完全透明。
实体字段上添加自定义注解 @CryptoField，标记该字段为敏感字段。
拦截器在数据写入前自动使用 SM4 加密该字段值，在数据读取后自动解密还原。Service 层和 Controller 层始终操作明文，无需感知加密过程。

实体类中加密字段的声明方式示例：

```java
// 实体类中敏感字段的标准写法
// @CryptoField
// private String phoneNumber;

// @CryptoField
// private String idCard;
```

### 全链路加密流程
前后端配合完成，核心逻辑为"SM4 加密数据 + SM2 保护密钥"。

前端发起请求时：
随机生成一个一次性 SM4 对称密钥
使用该 SM4 密钥对请求体明文进行加密，得到数据密文
使用预置的 SM2 公钥对该 SM4 密钥进行加密，得到密钥密文
将密钥密文放入自定义 HTTP 请求头，数据密文放入请求体，发送请求

后端收到请求时：
从请求头取出密钥密文，使用本地 SM2 私钥解密，还原出 SM4 密钥
使用该 SM4 密钥对请求体密文解密，得到明文数据
执行业务逻辑，生成响应数据
随机生成一个一次性 SM4 对称密钥
使用该 SM4 密钥对响应数据进行加密，得到数据密文
使用预置的 SM2 公钥对该 SM4 密钥进行加密，得到密钥密文
将密钥密文放入自定义 HTTP 响应头，数据密文放入响应体，返回响应

前端接收到响应时：
从响应头取出密钥密文，使用本地 SM2 私钥解密，还原出 SM4 密钥
该 SM4 密钥对响应数据进行解密，得到明文数据
执行业务逻辑


### Jackson 脱敏序列化器
通过自定义 Jackson 序列化器，在对象序列化为 JSON 时自动对敏感字段进行脱敏处理。
返回体在字段上添加自定义注解 @SensitiveField标记需要脱敏展示的字段

返回体中脱敏字段的声明方式示例：

```java
// 返回体中敏感字段的标准写法
// @SensitiveField(type = SensitiveType.PHONE)
// private String phoneNumber;

// @SensitiveField(type = SensitiveType.ID_CARD)
// private String idCard;
```

### log 模块详解
满足等保合规的审计日志模块。
操作日志防篡改机制：每条日志写入时计算自身内容的哈希值，并将上一条日志的哈希值作为输入之一。
任意一条日志被篡改会导致后续所有日志哈希验证失败。审计人员通过校验哈希链完整性判断日志是否被非法修改。
全链路追踪：通过 traceId 将一次请求从进入系统到返回响应的所有日志串联起来，便于问题排查和操作回溯。
审计日志通过注解声明记录。在需要审计的方法上添加 @Log 注解，框架自动记录操作人、操作时间、操作内容、操作结果等信息。
使用方式：

```java
// Service 层方法添加审计日志注解
// @AuditLog(module = "用户管理", operation = "新增用户", detail = "新增系统用户")
// public UserVO createUser(UserCreateDTO dto) {
// 业务逻辑
// }
```
### 后端技术栈
基础框架：Spring Boot 3、Lombok
安全框架：Spring Security 6、JWT、国密
数据库：MySQL、HikariCP、MyBatis-Plus
缓存框架：Redis、Spring-Cache、Redisson
文档生成：Smart-Doc
工具库：Validation、EasyExcel、Hutool
构建工具：Maven、Docker、Shell

### MyBatis-Plus 数据规范
逻辑删除：配置自动过滤已删除数据，执行删除操作时转换为 update 设置删除标记字段，所有查询自动附加过滤条件屏蔽已删除数据，防止物理删除风险和逻辑删除数据在业务界面可见。
乐观锁：通过 version 字段控制并发更新安全，更新时自动校验版本号，版本号不一致则拒绝本次更新，避免脏写。
自动填充：创建时间、更新时间、创建人、更新人等字段自动维护，无需在业务代码中手动设置。
分页插件：全局配置分页合理化，请求页码超过实际最大页数时自动返回最后一页。对单次查询最大记录数做限制，防止深度分页问题。
字段加密存储：实体字段上使用 @CryptoField 注解，通过 MyBatis 拦截器写入前加密、读取后解密，对业务代码完全透明。

### 统一接口响应规范
所有接口返回值必须使用统一封装体 Result。 完整类路径 com.xiyao.system.framework.common.Result
Result 结构：
code：int 类型，HTTP状态码。
msg：String 类型，提示信息
data：泛型 T 类型，返回的数据主体，无数据时为 null
traceId：String 类型，全链路追踪标识
禁止直接返回 entity 对象，Controller 返回的 data 类型必须是 VO 对象。

常用静态方法：

```java
// 成功，带数据
// Result.ok(data);

// 成功，无数据
// Result.ok();

// 失败，自定义提示信息
// Result.error("提示信息");

// 失败，自定义状态码和提示信息
// Result.error(500, "提示信息");
```
Controller 中使用范例：

```java
// 标准 Controller 方法写法
// @GetMapping("/users")
// public Result<Object> queryUsers(UserQueryDTO dto) {
// PageResult<UserVO> result = userService.queryUsers(dto);
// return Result.ok(result);
// }

// @PostMapping("/users")
// public Result<Object> createUser(@RequestBody @Valid UserCreateDTO dto) {
// userService.createUser(dto);
// return Result.ok();
// }
```


### 异常处理规范
全局异常处理器统一拦截所有未捕获异常 GlobalExceptionHandler，转换为标准 Result 格式返回。
业务异常类 BusinessException，使用时传入错误码和提示信息。
禁止在 Controller 中各自 try-catch 处理异常。
```java
// 业务代码中抛出业务异常
// throw new BusinessException(401, "用户名已存在");

```

### 可插拔配置约定
通过 Spring 的条件装配实现功能可插拔，每个独立功能对应的配置类使用 @ConditionalOnProperty 注解，配合 application.yml 中的配置项控制是否加载。
新增框架级能力扩展时，优先使用此方式进行可插拔设计。
配置类写法范例：

```java
// crypto 模块的条件装配配置类
// @Configuration
// @ConditionalOnProperty(name = "crypto-data.enable", havingValue = "true", matchIfMissing = true)
// public class CryptoAutoConfiguration {
// 配置内容
// }
```
application.yml 中的对应配置：

```yaml

crypto:
  enabled: true
governance:
  enabled: true

```

### 运维部署
容器化：Docker + docker-compose
Web 服务：Nginx（反向代理、静态资源托管）
更新流程：SSH 执行脚本 → Git 拉取最新代码 → 前端构建 → Maven 打包 → Docker 镜像构建 → 滚动更新容器。

### 开发规范
遵循阿里巴巴 Java 开发手册
代码注释语言使用中文，方法和文档注释使用标准 javadoc 格式
敏感数据必须使用 SM4 加密后存储，通过 @CryptoField 注解声明
接口响应统一封装格式（Result 包含 code、msg、data、traceId）
异常通过全局 @RestControllerAdvice 统一处理，返回标准错误码
审计日志通过 @AuditLog 注解记录，确保操作可追溯
禁止在 Controller 中编写复杂业务，业务逻辑均下沉至 service 层
禁止直接返回 entity 对象，必须返回 VO 对象
单元测试要求覆盖核心逻辑，提交前需通过

### 对话约定
当在 Dayplan 框架上下文中生成代码时，请遵循以下原则：
提供代码时注明应该放在哪个模块（system/service/app）
涉及加密相关功能，优先使用 SM2/SM3/SM4 国密算法
安全相关的修改会额外说明其等保合规意义和三员影响
框架级能力扩展建议优先使用 @ConditionalOnProperty 进行可插拔设计
目前属于框驾搭建阶段，controller、entity、service、vo 先放在 system/src/main/java/com/xiyao/system 子包下 app和service暂时不处理
