## auditLog 模块

### 模块介绍

```yaml
auditLog 模块提供符合等保合规的审计日志能力，记录用户操作和登录行为。

核心功能:
  - 操作日志：记录用户增删改查操作，含模块、类型、参数、结果、耗时
  - 登录日志：记录登录成功/失败、账号、IP、设备、地点
  - 防篡改：SM3 哈希链保证日志连续性，任何篡改可检测
  - 异步记录：基于 Spring Event 异步处理，不阻塞业务线程

技术特点:
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 事件驱动：登录事件订阅，与 security 模块解耦
  - 等保合规：三员权限隔离，审计日志仅审计员可查
```

---

### 技术实现方案

**技术栈：**

```yaml
日志切面: Spring AOP（@Around 环绕通知）
事件机制: Spring Event（ApplicationEventPublisher）
异步处理: @Async + 自定义线程池
哈希算法: SM3 国密哈希（防篡改）
```

**日志记录流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          日志记录流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  方法执行前 ──► LogAspect 拦截（@Log 注解）
                        │
                        ▼
              构建 LogOperationEvent 事件
                        │
                        ▼
              方法执行（try）- 记录结果
              方法异常（catch）- 记录异常
                        │
                        ▼
              finally - 设置耗时、发布时间
                        │
                        ▼
              SpringUtil.publishEvent() 发布事件
                        │
                        ▼
              LogListener @Async 异步接收
                        │
                        ▼
              保存到数据库（计算哈希链）
```

**登录事件订阅流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        登录事件流程                                   │
└─────────────────────────────────────────────────────────────────────┘

  LoginController.login() 认证成功
                        │
                        ▼
  构建 LogLoginEvent（用户ID、状态、IP、设备）
                        │
                        ▼
  SpringUtil.publishEvent() 发布事件
                        │
                        ▼
  LogListener 异步接收
                        │
                        ▼
  保存 sys_login_log 表
```

**哈希链防篡改机制：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                        哈希链机制                                    │
└─────────────────────────────────────────────────────────────────────┘

  第1条记录：hash1 = SM3(seed + id + user_id + ... + timestamp)
                 │
                 ▼
  第2条记录：hash2 = SM3(hash1 + id + user_id + ... + timestamp)
                 │
                 ▼
  第3条记录：hash3 = SM3(hash2 + id + user_id + ... + timestamp)

  验证：重新计算哈希链，对比存储的 hash 值
  篡改检测：任意记录被修改，后续所有 hash 不匹配
```

---

### 组件结构

```tree
com.xiyao.auditLog/
├── annotation/
│   └── Log.java                       # 操作日志注解
│                                       # - module: 操作模块
│                                       # - operationType: 操作类型
│                                       # - isSaveRequestData: 保存请求参数
│                                       # - isSaveResponseData: 保存响应数据
│
├── aspect/
│   └── LogAspect.java                 # AOP 切面
│                                       # - @Around("@annotation(auditLog)")
│                                       # - 构建事件、发布时间
│
├── enums/
│   ├── OperationStatus.java          # 操作状态（SUCCESS=0, FAIL=1）
│   └── OperationType.java            # 操作类型
│                                       # 0=OTHER 1=QUERY 2=INSERT
│                                       # 3=UPDATE 4=DELETE 5=EXPORT 6=IMPORT
│
├── event/
│   ├── LogLoginEvent.java            # 登录日志事件
│   │                                   # userId/username/status/message
│   │                                   # clientIp/os/browser/platform
│   │
│   └── LogOperationEvent.java        # 操作日志事件
│                                       # userId/username/adminType/module
│                                       # method/type/status/message
│                                       # requestParam/returnResult/costTime
│
└── listener/
    └── LogListener.java              # 日志监听器（@Async）
                                        # - saveLoginLog() 保存登录日志
                                        # - saveOperationLog() 保存操作日志
```

---

### API 接口清单

```yaml
# 审计日志接口（仅审计管理员可访问）
GET /audit/oper-auditLog/list    # 操作日志列表
GET /audit/oper-auditLog/{id}     # 操作日志详情
GET /audit/login-auditLog/list    # 登录日志列表
GET /audit/login-auditLog/{id}    # 登录日志详情
POST /audit/auditLog/verify       # 哈希链完整性校验

# 日志记录（通过注解自动记录，无需接口）
@AuditLog(module = "用户管理", operationType = OperationType.INSERT)
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
├── base/event/MyBaseEvent.java    # 事件基类
└── utils/Result.java              # 统一响应

# Security 模块（获取当前用户信息）
src/main/java/com/xiyao/security/
├── utils/SecurityUtils.java       # getUserId/getUsername/getAdminType
└── details/LoginUser.java        # 登录用户详情

# System 模块（实体和 Mapper）
src/main/java/com/xiyao/system/
├── entity/LogLogin.java           # 登录日志实体
├── entity/LogOperation.java      # 操作日志实体
└── mapper/LogLoginMapper.java     # 登录日志 Mapper
    └── mapper/LogOperationMapper.java # 操作日志 Mapper
```

---

### 关键表结构

```sql
-- 操作日志表
CREATE TABLE sys_oper_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT COMMENT '操作用户ID',
    username        VARCHAR(50) COMMENT '操作用户账号',
    admin_type      INT COMMENT '三员类型 0-普通用户 1-系统管理员 2-安全管理员 3-审计管理员',
    operation_module VARCHAR(100) COMMENT '操作模块',
    operation_type  INT COMMENT '操作类型 0-其他 1-查询 2-新增 3-更新 4-删除 5-导出 6-导入',
    operation_method VARCHAR(200) COMMENT '操作方法',
    request_param   TEXT COMMENT '请求参数',
    return_result   TEXT COMMENT '返回结果',
    status          INT DEFAULT 0 COMMENT '状态 0-失败 1-成功',
    message         VARCHAR(500) COMMENT '提示消息',
    cost_time       BIGINT DEFAULT 0 COMMENT '消耗时间（毫秒）',
    operation_time  DATETIME COMMENT '操作时间',
    hash            VARCHAR(64) NOT NULL COMMENT '本记录哈希值',
    prev_hash       VARCHAR(64) COMMENT '上一条记录哈希值',
    create_time     DATETIME COMMENT '创建时间'
);

-- 登录日志表
CREATE TABLE sys_login_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT COMMENT '用户ID',
    username        VARCHAR(50) COMMENT '登录账号',
    status          INT DEFAULT 0 COMMENT '状态 0-成功 1-失败',
    message         VARCHAR(255) COMMENT '提示消息',
    ip              VARCHAR(50) COMMENT '登录IP',
    os              VARCHAR(100) COMMENT '操作系统',
    browser         VARCHAR(100) COMMENT '浏览器',
    platform        VARCHAR(50) COMMENT '平台类型',
    login_time      DATETIME COMMENT '登录时间',
    hash            VARCHAR(64) NOT NULL COMMENT '本记录哈希值',
    prev_hash       VARCHAR(64) COMMENT '上一条记录哈希值',
    create_time     DATETIME COMMENT '创建时间'
);
```

---

### 配置文件

```yaml
# application.yml
# auditLog 模块配置（插件式，通过 @ConditionalOnProperty 控制）
# 无需额外配置，事件由 security 模块发布后自动处理
```

---

### 安全合规说明

```java
// 等保合规要求
// 1. 审计日志禁止物理删除，仅能逻辑删除
// 2. 审计日志禁止修改，仅审计管理员可查询
// 3. 登录成功/失败必须记录 IP 和地点
// 4. 哈希链机制确保日志完整性

// 三员权限
// - 操作日志查询：系统管理员、安全管理员
// - 登录日志查询：系统管理员、安全管理员、审计管理员
// - 哈希链校验：仅审计管理员
```