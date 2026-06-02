## system 模块

### 模块介绍

```yaml
system 模块是 DayPlan 框架的核心业务模块，提供用户、角色、菜单、字典、日志、系统配置、地址管理等系统级能力。

核心功能:
  - 用户管理：用户的增删改查、角色分配、密码重置、状态管理
  - 角色管理：角色 CRUD、菜单权限分配
  - 菜单管理：路由配置、组件映射、权限标识
  - 字典管理：数据字典（DictData）和字典类型（DictType）
  - 日志管理：操作日志、认证日志、审计日志
  - 系统配置：系统级配置项管理（逻辑删除）
  - 行政区划：行政区划数据管理（物理删除，递归子级）
  - 地址信息：地址数据管理（物理删除）

技术特点:
  - 继承 MyBaseEntity 获取审计字段（创建人/时间、更新人/时间）
  - MyBatis-Plus 通用 Mapper/Service 减少样板代码
  - @Log 注解 + AOP 自动记录操作日志
  - @DictBind 注解 + MyBatis 拦截器自动翻译字典值
  - 三员权限隔离（系统管理员、安全管理员、审计管理员）
  - 配置数据逻辑删除，保护系统配置安全
  - 行政区划/地址数据递归删除，确保层级数据一致性
```

---

### 技术实现方案

**技术栈：**

```yaml
数据访问: MyBatis-Plus + LambdaQueryWrapper
日志记录: @Log 注解 + LogAspect AOP
字典翻译: @DictBind 注解 + DictInterceptor
权限控制: @PreAuthorize SpEL 表达式
分页查询: IPage + PageResult
```

**模块结构：**

```ascii
┌─────────────────────────────────────────────────────────────────────┐
│                         system 模块结构                              │
└─────────────────────────────────────────────────────────────────────┘

  entity/           # 数据库实体（继承 MyBaseEntity）
  ├── SysUser.java     # 用户实体
  ├── SysRole.java     # 角色实体
  ├── SysMenu.java     # 菜单实体
  ├── DictData.java    # 字典数据
  ├── DictType.java    # 字典类型
  ├── LogOperation.java # 操作日志
  └── LogLogin.java    # 认证日志

  mapper/           # MyBatis-Plus Mapper 接口
  service/          # 业务逻辑接口
  service/impl/     # 业务逻辑实现
  controller/        # REST API 控制器
  vo/               # 视图对象

  ─────────────────────────────────────────────────

  Controller → Service → Mapper → DB
```

---

### 组件结构

```tree
com.xiyao.system/
├── entity/                          # 数据库实体
│   ├── SysUser.java                 # 用户实体
│   ├── SysRole.java                 # 角色实体
│   ├── SysMenu.java                 # 菜单实体
│   ├── SysRoleMenu.java             # 角色菜单关联
│   ├── SysUserRole.java             # 用户角色关联
│   ├── DictData.java                # 字典数据
│   ├── DictType.java                # 字典类型
│   ├── LogOperation.java            # 操作日志
│   ├── LogLogin.java               # 认证日志
│   ├── SysConfig.java               # 系统配置
│   ├── SysAddress.java              # 地址信息
│   └── SysRegions.java             # 区域信息

├── mapper/                          # MyBatis-Plus Mapper
│   ├── SysUserMapper.java
│   ├── SysRoleMapper.java
│   ├── SysMenuMapper.java
│   ├── SysRoleMenuMapper.java
│   ├── SysUserRoleMapper.java
│   ├── DictDataMapper.java
│   ├── DictTypeMapper.java
│   ├── LogOperationMapper.java
│   ├── LogLoginMapper.java
│   ├── SysConfigMapper.java
│   ├── SysAddressMapper.java
│   └── SysRegionsMapper.java       # 行政区划 Mapper

├── service/                         # 业务逻辑接口
│   ├── ISysUserService.java
│   ├── ISysRoleService.java
│   ├── ISysMenuService.java
│   ├── IDictDataService.java
│   ├── IDictTypeService.java
│   ├── ILogLoginService.java
│   ├── ILogOperationService.java
│   ├── ISysConfigService.java      # 系统配置服务
│   ├── ISysRegionsService.java      # 行政区划服务
│   └── ISysAddressService.java      # 地址信息服务

├── service/impl/                   # 业务逻辑实现
│   ├── SysUserServiceImpl.java
│   ├── SysRoleServiceImpl.java
│   ├── SysMenuServiceImpl.java
│   ├── DictDataServiceImpl.java
│   ├── DictTypeServiceImpl.java
│   ├── LogLoginServiceImpl.java
│   ├── LogOperationServiceImpl.java
│   ├── SysConfigServiceImpl.java    # 系统配置服务实现
│   ├── SysRegionsServiceImpl.java   # 行政区划服务实现
│   └── SysAddressServiceImpl.java  # 地址信息服务实现

├── controller/                      # REST API 控制器
│   ├── SysUserController.java      # 用户管理
│   ├── SysRoleController.java      # 角色管理
│   ├── SysMenuController.java      # 菜单管理
│   ├── DictDataController.java     # 字典数据
│   ├── DictTypeController.java     # 字典类型
│   ├── LogLoginController.java     # 认证日志
│   ├── LogOperationController.java # 操作日志
│   ├── SysConfigController.java    # 系统配置
│   ├── SysRegionsController.java   # 行政区划
│   └── SysAddressController.java   # 地址信息

└── vo/                             # 视图对象
    ├── SysUserVo.java
    ├── SysRoleVo.java
    ├── SysMenuVo.java
    ├── AssignRolesVo.java          # 分配角色
    ├── AssignMenusVo.java         # 分配菜单
    ├── DictDataVo.java
    ├── DictTypeVo.java
    ├── LogLoginVo.java
    ├── LogOperationVo.java
    ├── SysConfigVo.java            # 系统配置
    ├── SysRegionsVo.java           # 行政区划
    └── SysAddressVo.java           # 地址信息
```

---

### API 接口清单

#### 用户管理接口

```yaml
GET    /system/user/list            # 用户列表（分页+条件）
GET    /system/user/{id}            # 用户详情
POST   /system/user                 # 创建用户
PUT    /system/user                 # 更新用户
DELETE /system/user/{id}          # 删除用户（逻辑删除）
PUT    /system/user/assign-roles   # 分配角色
PUT    /system/user/reset-pwd     # 重置密码（仅系统管理员）
PUT    /system/user/status        # 修改状态

# 权限：仅三员（系统管理员/安全管理员）可访问
```

#### 角色管理接口

```yaml
GET    /system/role/list            # 角色列表
GET    /system/role/{id}           # 角色详情
POST   /system/role               # 创建角色
PUT    /system/role               # 更新角色
DELETE /system/role/{id}          # 删除角色
GET    /system/role/menus/{roleId} # 角色已分配菜单
PUT    /system/role/assign-menus  # 分配菜单

# 权限：仅三员可访问
```

#### 菜单管理接口

```yaml
GET    /system/menu/list          # 菜单列表（树形）
GET    /system/menu/{id}          # 菜单详情
POST   /system/menu               # 创建菜单
PUT    /system/menu               # 更新菜单
DELETE /system/menu/{id}          # 删除菜单
GET    /system/menu/options       # 菜单下拉选项

# 权限：仅三员可访问
```

#### 字典管理接口

```yaml
# 字典类型
GET    /system/dict/type/list      # 字典类型列表
GET    /system/dict/type/{id}     # 字典类型详情
POST   /system/dict/type          # 创建字典类型
PUT    /system/dict/type          # 更新字典类型
DELETE /system/dict/type/{id}     # 删除字典类型

# 字典数据
GET    /system/dict/data/list      # 字典数据列表
GET    /system/dict/data/{id}     # 字典数据详情
GET    /system/dict/data/options/{dictType} # 字典数据下拉选项
POST   /system/dict/data          # 创建字典数据
PUT    /system/dict/data          # 更新字典数据
DELETE /system/dict/data/{id}     # 删除字典数据
POST   /system/dict/data/refresh # 刷新字典缓存
```

#### 日志查询接口

```yaml
# 认证日志
GET    /system/log-login/list      # 认证日志列表

# 操作日志
GET    /system/log-operation/list  # 操作日志列表

# 权限：
# - 审计管理员：可查看所有日志
# - 其他管理员：只能查看自己的
```

#### 系统配置接口

```yaml
GET    /system/config/list          # 配置列表
GET    /system/config/{id}          # 配置详情
GET    /system/config/key/{name}   # 根据名称获取配置值
POST   /system/config              # 创建配置
PUT    /system/config              # 更新配置
DELETE /system/config/{id}         # 删除配置（逻辑删除）

# 权限：仅系统管理员可访问
# 特性：采用逻辑删除机制，删除操作将 deleted 字段置为 1
```

#### 行政区划接口

```yaml
GET    /system/regions/list        # 行政区划列表
GET    /system/regions/{code}      # 行政区划详情
POST   /system/regions            # 创建行政区划
PUT    /system/regions            # 更新行政区划
DELETE /system/regions/{code}     # 删除行政区划（递归删除子级）
GET    /system/regions/children/{parentCode}  # 获取子级行政区划
GET    /system/regions/level/{level}          # 按级别获取行政区划

# 权限：
# - 查询接口：公开访问
# - 增删改接口：仅系统管理员可访问
# 特性：删除时递归删除所有下级子节点
```

#### 地址信息接口

```yaml
GET    /system/address/list        # 地址列表
GET    /system/address/{code}      # 地址详情
POST   /system/address            # 创建地址
PUT    /system/address            # 更新地址
DELETE /system/address/{code}   # 删除地址（物理删除）
GET    /system/address/children/{parentCode}  # 获取子级地址
GET    /system/address/level/{level}          # 按级别获取地址

# 权限：
# - 查询接口：公开访问
# - 增删改接口：仅系统管理员可访问
# 特性：物理删除，不支持递归删除子级
```

---

### 关键表结构

```sql
-- 用户表
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50) NOT NULL UNIQUE COMMENT '用户账号',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    salt        VARCHAR(50) COMMENT '密码盐值',
    mobile      VARCHAR(20) COMMENT '手机号',
    nick_name   VARCHAR(50) COMMENT '昵称',
    email       VARCHAR(100) COMMENT '邮箱',
    sex         INT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    avatar      VARCHAR(255) COMMENT '头像',
    login_ip    VARCHAR(50) COMMENT '最后登录IP',
    login_date  DATETIME COMMENT '最后登录时间',
    status      INT DEFAULT 1 COMMENT '状态 0-停用 1-正常',
    remark      VARCHAR(500) COMMENT '备注',
    deleted     INT DEFAULT 0 COMMENT '逻辑删除',
    version     INT DEFAULT 0 COMMENT '乐观锁',
    create_time DATETIME,
    update_time DATETIME
) COMMENT '系统用户';

-- 角色表
CREATE TABLE sys_role (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    type        INT DEFAULT 0 COMMENT '三员类型',
    sort        INT DEFAULT 0 COMMENT '排序',
    status      INT DEFAULT 0 COMMENT '状态',
    remark      VARCHAR(500) COMMENT '备注',
    create_time DATETIME,
    update_time DATETIME
) COMMENT '系统角色';

-- 菜单表
CREATE TABLE sys_menu (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name        VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type        CHAR(1) DEFAULT 'M' COMMENT 'M-目录 C-菜单 F-按钮',
    path        VARCHAR(200) COMMENT '路由地址',
    component   VARCHAR(255) COMMENT '组件路径',
    perms       VARCHAR(100) COMMENT '权限标识',
    icon        VARCHAR(100) COMMENT '图标',
    sort        INT DEFAULT 0 COMMENT '排序',
    status      INT DEFAULT 0 COMMENT '状态',
    remark      VARCHAR(500) COMMENT '备注',
    create_time DATETIME,
    update_time DATETIME
) COMMENT '系统菜单';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联';

-- 操作日志表
CREATE TABLE log_operation (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT COMMENT '操作用户ID',
    username         VARCHAR(50) COMMENT '操作用户账号',
    admin_type       INT COMMENT '三员类型',
    operation_module VARCHAR(100) COMMENT '操作模块',
    operation_method VARCHAR(200) COMMENT '操作方法',
    operation_type   INT COMMENT '操作类型',
    status           INT DEFAULT 0 COMMENT '状态',
    message          VARCHAR(500) COMMENT '消息',
    cost_time        BIGINT DEFAULT 0 COMMENT '耗时',
    ip               VARCHAR(50) COMMENT '客户端IP',
    trace_id         VARCHAR(32) COMMENT '链路追踪ID',
    log_type         INT COMMENT '日志类型',
    hash             VARCHAR(64) COMMENT 'SM3哈希值',
    prev_hash        VARCHAR(64) COMMENT '上一条哈希',
    operation_time   DATETIME COMMENT '操作时间'
) COMMENT '操作日志';

-- 认证日志表
CREATE TABLE log_login (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT COMMENT '用户ID',
    username   VARCHAR(50) COMMENT '账号',
    auth_type  INT COMMENT '认证类型',
    status     INT DEFAULT 0 COMMENT '状态',
    message    VARCHAR(255) COMMENT '消息',
    ip         VARCHAR(50) COMMENT '客户端IP',
    trace_id   VARCHAR(32) COMMENT '链路追踪ID',
    login_time DATETIME COMMENT '认证时间',
    hash       VARCHAR(64) NOT NULL COMMENT 'SM3哈希值',
    prev_hash  VARCHAR(64) COMMENT '上一条哈希'
) COMMENT '认证日志';
```

---

### 依赖文件路径

```tree
# Common 基础类（被其他模块继承）
src/main/java/com/xiyao/common/
├── base/
│   ├── controller/MyBaseController.java    # 控制器基类
│   ├── entity/MyBaseEntity.java          # 实体基类
│   ├── mapper/MyBaseMapper.java           # Mapper 基类
│   └── service/impl/MyBaseServiceImpl.java # Service 实现基类
├── utils/
│   ├── Result.java                       # 统一响应封装
│   └── data/PageResult.java              # 分页结果封装
└── enums/
    └── Status.java                       # 状态枚举

# Framework 配置模块
src/main/java/com/xiyao/framework/
├── config/MybatisPlusConfig.java        # MyBatis-Plus 配置
├── exception/GlobalExceptionHandler.java # 全局异常处理
└── annotation/CurrentUser.java          # 当前用户注解

# Security 安全模块
src/main/java/com/xiyao/security/
├── config/SecurityConfig.java            # Spring Security 配置
├── utils/SecurityUtils.java             # 安全工具类
└── utils/JwtUtils.java                   # JWT 工具类

# Log 日志模块
src/main/java/com/xiyao/log/
├── annotation/Log.java                  # 日志注解
├── aspect/LogAspect.java                 # 日志切面
└── event/LogOperationEvent.java         # 操作日志事件

# Dict 字典模块
src/main/java/com/xiyao/dict/
├── annotation/DictBind.java              # 字典绑定注解
├── interceptor/DictInterceptor.java     # 字典拦截器
└── utils/DictCache.java                  # 字典缓存

# Crypto 加密模块
src/main/java/com/xiyao/crypto/
├── annotation/CryptoField.java          # 加密字段注解
└── core/EncryptorManager.java            # 加密管理器
```

---

### 三员权限说明

```java
// 三员类型
// 0 = 普通用户（NormalUser）
// 1 = 系统管理员（SystemAdmin）
// 2 = 安全管理员（SecurityAdmin）
// 3 = 审计管理员（AuditAdmin）

// 三员职责
// 系统管理员：系统配置、用户账号管理，不能查看审计日志
// 安全管理员：权限分配、安全策略，不能查看审计日志
// 审计管理员：查看审计日志，监督其他两员操作，无配置权限
```

**接口权限控制：**

```java
// @PreAuthorize("@ss.isSystemAdmin()") - 仅系统管理员
// @PreAuthorize("@ss.hasAnyAdmin()")   - 系统管理员或安全管理员
// @PreAuthorize("@ss.isAuditAdmin()")   - 仅审计管理员
```

---

### 配置说明

```yaml
# 无需特殊配置，system 模块使用框架默认值

# 如果需要自定义，可以覆盖：
# mybatis-plus:
#   mapper-locations: classpath*:mapper/**/*.xml
#   type-aliases-package: com.xiyao.system.entity
```