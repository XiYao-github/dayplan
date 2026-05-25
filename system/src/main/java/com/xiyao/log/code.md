## security模块

- RBAC 权限管理（含三员管理）模块方案

### 功能描述

- 操作日志：记录用户增删改查操作，含模块、操作类型、请求参数、响应结果
- 登录日志：记录登录成功/失败、账号、IP、地点、设备
- 防篡改：使用SM3哈希链保证日志连续性，任何篡改可检测
- 异步记录：基于Spring Event异步处理，不阻塞业务线程
- 查询权限：仅审计员可查看和导出审计日志

### 哈希链防篡改机制

- 每条日志记录生成一个SM3哈希值
- 当前记录 hash = SM3(prev_hash + id + user_id + module + operation + request_param + result + timestamp)
- 表内第一条记录的 prev_hash 设为固定种子值（如'0000...'）
- 提供验证接口：审计员可对日志链进行完整性校验，检测是否有记录被删除或修改

### 事件发布与监听流程

- 方法执行前，LogAspect 获取注解信息
- 方法执行后，构造 LogOperationEvent 或 LogLoginEvent
- 使用 Spring ApplicationEventPublisher 发布事件
- LogListener 使用 @Async 异步接收事件
- 监听器中调用 Service 保存日志到数据库，保存前计算哈希值（通过获取上一条记录的hash和当前内容）

### 安全合规说明

- 审计日志表禁止任何用户直接修改或物理删除（仅审计员可查询）
- 定期自动备份审计日志
- 登录成功/失败记录IP和地点，满足等保对登录行为审计的要求

### 核心组件位置

- system/src/main/java/com/xiyao/system 实体类相关文件在这个包下

```
system/src/main/java/com/xiyao/log/
├── annotation/
│   └── Log.java                       # 日志注解
├── aspect/
│   └── LogAspect.java                 # AOP切面，解析注解并发布事件
├── enums/
│   ├── OperationStatus.java           # 操作状态（SUCCESS, FAIL）
│   └── OperationType.java             # 操作类型（INSERT, UPDATE, DELETE, QUERY, LOGIN, EXPORT等）
├── event/
│   ├── LogLoginEvent.java             # 登录日志事件
│   └── LogOperationEvent.java        # 操作日志事件
├── listener/
│   └── LogListener.java              # 日志监听器，异步保存日志并计算哈希
├── service/
│   ├── IOperLogService.java           # 操作日志服务接口
│   ├── ILoginLogService.java          # 登录日志服务接口
│   └── impl/
│       ├── OperLogServiceImpl.java
│       └── LoginLogServiceImpl.java
└── controller/
    └── AuditLogController.java        # 审计日志查询（仅AUDIT_ADMIN可访问）
```

### 配置示例

- application.yml

```
dayplan:
  crypto:
    enabled: true
    sm2:
      public-key: classpath:keys/sm2_public.key
      private-key: classpath:keys/sm2_private.key
    sm4:
      key: classpath:keys/sm4.key
      mode: CBC
      padding: PKCS5Padding
```

### 关键表结构

- sys_oper_log：操作日志表
- sys_login_log：登录日志表

```
-- 操作日志表
CREATE TABLE sys_oper_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT COMMENT '操作用户ID',
    username    VARCHAR(50) COMMENT '操作用户账号',
    module      VARCHAR(100) COMMENT '操作模块',
    operation   VARCHAR(100) COMMENT '操作类型',
    method      VARCHAR(200) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_param TEXT COMMENT '请求参数（脱敏后）',
    result      TEXT COMMENT '返回结果',
    status      INT DEFAULT 0 COMMENT '操作状态 0成功 1失败',
    error_msg   TEXT COMMENT '错误信息',
    ip          VARCHAR(50) COMMENT '操作IP',
    location    VARCHAR(100) COMMENT '操作地点',
    cost_time   BIGINT DEFAULT 0 COMMENT '耗时（毫秒）',
    trace_id    VARCHAR(64) COMMENT '链路追踪ID',
    hash        VARCHAR(64) NOT NULL COMMENT '本记录哈希值',
    prev_hash   VARCHAR(64) COMMENT '上一条记录哈希值',
    create_time DATETIME COMMENT '创建时间'
) COMMENT '操作日志表';

-- 登录日志表
CREATE TABLE sys_login_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50) COMMENT '登录账号',
    status      INT DEFAULT 0 COMMENT '登录状态 0成功 1失败',
    fail_reason VARCHAR(255) COMMENT '失败原因',
    ip          VARCHAR(50) COMMENT '登录IP',
    location    VARCHAR(100) COMMENT '登录地点',
    device      VARCHAR(100) COMMENT '设备信息',
    create_time DATETIME COMMENT '登录时间'
) COMMENT '登录日志表';

```