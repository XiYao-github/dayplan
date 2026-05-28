## security 模块

### 模块介绍

```yaml
security 模块是 DayPlan 框架的安全核心模块，为整个系统提供认证和授权能力。

核心功能:
  - 身份认证：用户名密码登录、注册、退出，JWT Token 无状态认证
  - 权限控制：基于 RBAC 的细粒度权限管理，支持菜单级和按钮级权限
  - 三员分立：等保合规的三权分立体系，系统管理员、安全管理员、审计管理员相互制约
  - 技术支撑：不包含业务逻辑，为业务层提供安全技术能力，可插拔式设计

技术特点:
  - 无状态认证：JWT Token + Redis 会话存储，支持分布式部署
  - 插件式配置：通过 @ConditionalOnProperty 实现功能可插拔
  - 国密支持：BCrypt 密码加密（可扩展 SM2/SM4）
  - 事件驱动：登录事件发布，与日志模块解耦
```

---

### 技术实现方案

**技术栈：**

```yaml
认证框架: Spring Security 6
会话管理: JWT Token + Redis
密码加密: BCrypt 单向加密
权限模型: RBAC（用户-角色-菜单）
等保合规: 三员分立体系
事件机制: Spring Event（登录事件）
```

**核心流程：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          身份认证流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  POST /login {username, password}
        │
        ▼
  AuthenticationManager.authenticate()
        │
        ▼
  UserDetailsServiceImpl.loadUserByUsername()
        │
        ├── 查询 sys_user（基本信息、状态）
        ├── 查询 sys_user_role（角色列表）
        ├── 查询 sys_role_menu（菜单列表）
        ├── 查询 sys_menu（权限标识 perms）
        └── 查询 sys_role（三员类型）
        │
        ▼
  构建 LoginUser（UserDetails 实现）
        │
        ▼
  JwtUtils.getToken() 生成 Token
        │
        ├── 生成随机 UUID 作为 Redis key
        ├── LoginUser 缓存到 Redis
        └── JWT Token payload 包含 key
        │
        ▼
  返回 Token 给客户端

  ────────────────────────────────────────

  后续请求携带 Token
        │
        ▼
  JwtAuthenticationFilter 拦截
        │
        ├── 从 Header 提取 Token
        ├── JwtUtils.validateToken() 验证
        ├── JwtUtils.getLoginUser() 获取用户
        └── SecurityContextHolder 设置认证信息
```

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          权限校验流程                                 │
└─────────────────────────────────────────────────────────────────────┘

  请求进入 Security Filter Chain
        │
        ▼
  JwtAuthenticationFilter 解析 Token
        │
        ▼
  设置 SecurityContextHolder
        │
        ▼
  @PreAuthorize 权限注解判断
        │
        ├── @PreAuthorize("hasRole('ADMIN')")
        ├── @PreAuthorize("@ss.isSystemAdmin()")
        └── @PreAuthorize("@ss.hasAnyAdmin()")
        │
        ▼
  AccessDeniedHandler / AuthenticationEntryPoint 处理
```

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                          三员权限体系                                 │
└─────────────────────────────────────────────────────────────────────┘

  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
  │  系统管理员   │    │  安全管理员   │    │  审计管理员   │
  │   Type=1    │    │   Type=2    │    │   Type=3    │
  ├─────────────┤    ├─────────────┤    ├─────────────┤
  │  系统配置    │     │  权限分配    │    │  查看审计日志 │
  │  用户账号    │     │  安全策略    │    │  监督两员操作 │
  │  数据操作    │     │  角色管理    │    │  无配置权限   │
  └─────────────┘    └─────────────┘    └─────────────┘
        │                  │                  │
        └──────────────────┴──────────────────┘
                           │
                    权限互斥，操作分离
```

---

### 组件结构

```tree
com.xiyao.security/
├── config/
│   └── SecurityConfig.java
│       # Spring Security 核心配置
│       # - 过滤器链配置（JWT Filter 位置）
│       # - 请求授权规则（三员路径隔离）
│       # - 会话管理（无状态 STATELESS）
│       # - 异常处理（401/403）
│       # - 密码编码器（BCrypt）
│       # - 认证管理器（AuthenticationManager）
│
├── controller/
│   └── LoginController.java
│       # 登录控制器
│       # - POST /login 用户登录
│       # - POST /register 用户注册
│       # - POST /logout 用户退出
│
├── details/
│   ├── LoginUser.java
│   │   # 登录用户详情（实现 UserDetails）
│   │   # - userId: 用户ID
│   │   # - adminType: 三员类型
│   │   # - sysUser: 用户实体
│   │   # - permissions: 权限标识集合
│   │   # - getAuthorities(): 权限集合
│   │
│   └── UserVo.java
│       # 登录请求对象
│       # - username: 用户名
│       # - password: 密码
│       # - code: 验证码
│
├── enums/
│   └── AdminType.java
│       # 三员类型枚举
│       # - NormalUser = 0 普通用户
│       # - SystemAdmin = 1 系统管理员
│       # - SecurityAdmin = 2 安全保密管理员
│       # - AuditAdmin = 3 安全审计管理员
│
├── filter/
│   └── JwtAuthenticationFilter.java
│       # JWT 认证过滤器（OncePerRequestFilter）
│       # - 继承 OncePerRequestFilter
│       # - doFilterInternal(): 核心过滤方法
│       # - getHeaderToken(): 从 Header 提取 Token
│       # - validateToken(): 验证 Token 有效性
│       # - getLoginUser(): 获取缓存用户信息
│
├── handler/
│   ├── AccessDeniedHandlerImpl.java
│   │   # 权限不足处理器（返回 403）
│   │   # 实现 AccessDeniedHandler
│   │
│   └── AuthenticationEntryPointImpl.java
│       # 认证失败处理器（返回 401）
│       # 实现 AuthenticationEntryPoint
│
├── properties/
│   └── SecurityData.java
│       # 安全配置属性类
│       # @ConfigurationProperties(prefix = "security-data")
│       # - enabled: 是否启用
│       # - includePaths: 放行路径
│       # - staticPaths: 静态资源路径
│       # - jwt.secret: 签名密钥
│       # - jwt.expire: 过期时间
│
├── service/
│   ├── SecurityService.java
│   │   # 权限服务（SpEL 表达式调用）
│   │   # - isLogin(): 是否已登录
│   │   # - isSystemAdmin(): 是否系统管理员
│   │   # - isSecurityAdmin(): 是否安全管理员
│   │   # - isAuditAdmin(): 是否审计管理员
│   │   # - hasAnyAdmin(): 是否具有三员权限
│   │   # - hasRole(String): 是否有指定角色
│   │   # - hasAnyRole(String...): 是否有任一角色
│   │   # - hasAuthority(String): 是否有指定权限
│   │
│   └── UserDetailsServiceImpl.java
│       # 用户信息加载服务（实现 UserDetailsService）
│       # - loadUserByUsername(): 核心加载方法
│       # - 查询用户、角色、菜单、权限
│       # - 构建 LoginUser 返回
│
└── utils/
    ├── JwtUtils.java
    │   # JWT 工具类
    │   # - LOGIN_TOKEN: 载荷 key
    │   # - LOGIN_USER_KEY: Redis key 前缀
    │   # - getToken(LoginUser): 生成 Token
    │   # - validateToken(String): 验证 Token
    │   # - getLoginUser(String): 获取用户
    │   # - removeToken(String): 移除 Token
    │
    └── SecurityUtils.java
        # 安全工具类（静态方法）
        # - getLoginUser(): 获取当前用户
        # - getUserId(): 获取用户ID
        # - getUsername(): 获取用户名
        # - getAdminType(): 获取三员类型
        # - isSystemAdmin(): 是否系统管理员
        # - isSecurityAdmin(): 是否安全管理员
        # - isAuditAdmin(): 是否审计管理员
        # - hasAdminPermission(): 是否具有三员权限
```

---

### API 接口清单

```yaml
# Security 模块（登录认证）
POST   /login                     # 用户登录
POST   /register                 # 用户注册
DELETE /logout                    # 用户退出

  # System 模块（用户角色菜单管理，由 security 提供技术支持）
  # 完整接口参见 system 模块文档

  # SysUserController
GET    /system/user/list          # 用户列表
GET    /system/user/{id}          # 用户详情
POST   /system/user               # 创建用户
PUT    /system/user               # 更新用户
DELETE /system/user/{id}          # 删除用户
PUT    /system/user/assign-roles  # 分配角色
PUT    /system/user/reset-pwd     # 重置密码
PUT    /system/user/status        # 修改状态

  # SysRoleController
GET    /system/role/list          # 角色列表
GET    /system/role/{id}          # 角色详情
POST   /system/role               # 创建角色
PUT    /system/role               # 更新角色
DELETE /system/role/{id}          # 删除角色
GET    /system/role/menus/{roleId} # 角色已分配菜单
PUT    /system/role/assign-menus  # 分配菜单

  # SysMenuController
GET    /system/menu/list          # 菜单列表（树形）
GET    /system/menu/{id}          # 菜单详情
POST   /system/menu               # 创建菜单
PUT    /system/menu               # 更新菜单
DELETE /system/menu/{id}          # 删除菜单
GET    /system/menu/options       # 菜单下拉选项

  # 日志查询接口
GET    /system/log-login/list     # 认证日志列表
GET    /system/log-operation/list # 操作日志列表

# 日志权限说明
# 认证日志：审计管理员查看全部，其他管理员只能查看自己的
# 操作日志：审计管理员查看全部（含AUDIT），其他管理员只能查看OPERATION类型且只能查看自己的
```

---

### 依赖文件路径

```tree
# Base 基础类
src/main/java/com/xiyao/common/
├── base/
│   ├── controller/MyBaseController.java    # 控制器基类（success/error/Result）
│   ├── entity/MyBaseEntity.java          # 实体基类（公共字段）
│   ├── mapper/MyBaseMapper.java           # Mapper 基类
│   ├── service/MyBaseService.java         # Service 接口基类
│   └── service/impl/MyBaseServiceImpl.java # Service 实现基类
├── utils/
│   ├── Result.java                       # 统一响应封装
│   ├── RedisUtils.java                   # Redis 工具类
│   └── SpringUtils.java                  # Spring 工具类
└── enums/Status.java                     # 通用状态枚举

# System 业务实体（security 模块查询使用）
src/main/java/com/xiyao/system/
├── entity/
│   ├── SysUser.java          # 用户实体
│   ├── SysRole.java          # 角色实体
│   ├── SysMenu.java          # 菜单实体
│   ├── SysUserRole.java      # 用户角色关联
│   └── SysRoleMenu.java      # 角色菜单关联
├── mapper/
│   ├── SysUserMapper.java
│   ├── SysRoleMapper.java
│   ├── SysMenuMapper.java
│   ├── SysUserRoleMapper.java
│   └── SysRoleMenuMapper.java
└── service/                  # 业务 Service（用户角色菜单 CRUD）

# Log 模块（登录事件订阅）
src/main/java/com/xiyao/auditLog/
├── event/LogLoginEvent.java   # 登录事件
├── enums/OperationStatus.java # 操作状态枚举
└── listener/LogListener.java # 事件监听器
```

```java
// AuthenticationEntryPointImpl - 认证失败（未登录、Token 无效）
// 触发场景：
//   - 用户未登录，直接访问需要认证的接口
//   - JWT Token 已过期
//   - JWT Token 签名验证失败
//   - Token 在 Redis 中已失效
// 响应：HTTP 200, {"code": 401, "msg": "认证失败,请重新登录."}

// AccessDeniedHandlerImpl - 权限不足（已登录但无权限）
// 触发场景：
//   - 用户已登录，但访问的接口需要更高角色权限
//   - @PreAuthorize 权限校验失败
// 响应：HTTP 200, {"code": 403, "msg": "权限不足，无法访问."}
```

---

### 配置文件配置

```yaml
# application.yml
security-data:
  enabled: true                    # 是否启用安全过滤
  include-paths: # 无需认证的路径
    - /login
    - /register
    - /code
    - /oauth/**
  static-paths: # 静态资源路径
    - /static/**
    - /public/**
  jwt:
    secret: your-secret-key-here-must-be-at-least-32-characters  # 签名密钥
    expire: 7200                  # Token 过期时间（秒）

# 配置说明
# enabled: true 时启用 Security 安全过滤，false 时跳过所有安全配置
# include-paths: 配置无需认证即可访问的接口路径，支持 Ant 风格路径表达式
# jwt.secret: JWT 签名密钥，建议使用复杂字符串，长度至少 32 字符
# jwt.expire: Token 过期时间，默认 7200 秒（2 小时）
```

---

### 关键表结构

```sql
-- 表关系
sys_user ─────┬───── sys_user_role ───── sys_role
              │              │
              │              └───── sys_role_menu
              │                           │
              └───────────────────────────┘
                                          │
                                    sys_menu

-- 用户表
CREATE TABLE sys_user
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户账号',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    status      TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    remark      VARCHAR(500) COMMENT '备注'
) COMMENT '用户表';

-- 角色表
CREATE TABLE sys_role
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    type        INT     DEFAULT 0 COMMENT '三员类型 0-普通用户 1-系统管理员 2-安全管理员 3-审计管理员',
    sort        INT     DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-停用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    remark      VARCHAR(500) COMMENT '备注'
) COMMENT '角色表';

-- 菜单表
CREATE TABLE sys_menu
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT  DEFAULT 0 COMMENT '父菜单ID',
    name        VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type        CHAR(1) DEFAULT 'M' COMMENT 'M-目录 C-菜单 F-按钮',
    path        VARCHAR(200) COMMENT '路由地址',
    component   VARCHAR(255) COMMENT '组件路径',
    perms       VARCHAR(100) COMMENT '权限标识',
    icon        VARCHAR(100) COMMENT '图标',
    sort        INT     DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 0 COMMENT '状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    remark      VARCHAR(500) COMMENT '备注'
) COMMENT '菜单表';

-- 用户角色关联表
CREATE TABLE sys_user_role
(
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu
(
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联表';
```

---

### 三员类型说明

```java
// AdminType{
//   NormalUser=0,     // 普通用户
//   SystemAdmin=1,    // 系统管理员
//   SecurityAdmin=2,  // 安全保密管理员
//   AuditAdmin=3      // 安全审计管理员
// }
```

**三员职责说明：**

```java
// 系统管理员（SystemAdmin）
// 职责：系统配置、用户账号管理
// 权限：可操作业务数据，不能查看审计日志

// 安全保密管理员（SecurityAdmin）
// 职责：安全策略配置、权限管理
// 权限：角色管理、权限分配，不能查看审计日志

// 审计管理员（AuditAdmin）
// 职责：日志审计、操作记录查看
// 权限：监督两员操作，无配置权限
```

**等保合规要求：**三者权限互斥，相互制约，任何一人无法完成超越职责的操作。

---

### SecurityService 使用示例

SecurityService 提供 SpEL 表达式调用的权限校验方法，用于 @PreAuthorize 注解。

```java
// 在 Controller 或 Service 方法上加注解
// @PreAuthorize("@ss.isLogin()")
// @GetMapping("/user")
// public Result<UserVO> getUser() {
//     // 需要登录才能访问
// }

// @PreAuthorize("@ss.isSystemAdmin()")
// @DeleteMapping("/user/{id}")
// public Result<Void> deleteUser(@PathVariable Long id) {
//     // 只有系统管理员能删除
// }

// @PreAuthorize("@ss.hasAnyRole('admin', 'user')")
// @PostMapping("/resource")
// public Result<Void> createResource() {
//     // admin 或 user 角色能创建
// }

// @PreAuthorize("@ss.hasAuthority('system:user:delete')")
// @DeleteMapping("/users")
// public Result<Void> deleteUsers() {
//     // 需要 system:user:delete 权限
// }

// @PreAuthorize("@ss.hasAnyAdmin()")
// @PostMapping("/admin/action")
// public Result<Void> adminAction() {
//     // 系统管理员或安全管理员能访问
// }
```

**方法说明：**

```java
// 判断是否已登录
// @PreAuthorize("@ss.isLogin()")

// 获取当前登录用户 ID（用于记录操作人）
// Long userId = ss.getLoginUserId();

// 获取当前登录用户（获取用户详细信息）
// LoginUser user = ss.getLoginUser();

// 判断是否普通用户（三员类型为 0）
// @PreAuthorize("@ss.isNormalUser()")

// 判断是否系统管理员（三员类型为 1）
// @PreAuthorize("@ss.isSystemAdmin()")

// 判断是否安全管理员（三员类型为 2）
// @PreAuthorize("@ss.isSecurityAdmin()")

// 判断是否审计管理员（三员类型为 3）
// @PreAuthorize("@ss.isAuditAdmin()")

// 判断是否三员权限（系统管理员或安全管理员）
// @PreAuthorize("@ss.hasAnyAdmin()")

// 判断是否有指定角色
// @PreAuthorize("@ss.hasRole('admin')")

// 判断是否有任一角色
// @PreAuthorize("@ss.hasAnyRole('admin', 'user')")

// 判断是否有指定权限
// @PreAuthorize("@ss.hasAuthority('system:user:delete')")
```