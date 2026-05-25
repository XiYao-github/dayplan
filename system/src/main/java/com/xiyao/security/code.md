## security模块
- RBAC 权限管理（含三员管理）模块方案

### 功能描述
- 用户管理：支持用户增删改查，密码重置，状态启用/停用
- 角色管理：支持角色创建、编辑、分配菜单/按钮权限
- 菜单管理：支持目录、菜单、按钮三级结构，动态路由渲染
- 三员管理：系统管理员、安全保密管理员、安全审计管理员三者权限互斥、操作分离

### 技术实现方案
- 认证框架：Spring Security 6 + JWT
- 权限模型：RBAC（用户、角色、菜单、关联表）
- 三员管理：预置三个角色 + Security 拦截敏感操作 + 审计日志写入保护
- 后端校验：方法级权限控制（@PreAuthorize），前端动态路由配合

### 权限隔离实现说明
- 在 SecurityConfig 中配置三员访问控制：
- 审计相关接口（/audit/）仅 AUDIT_ADMIN 可访问
- 系统配置类接口（/config/）仅 SYSTEM_ADMIN 可访问
- 用户管理、角色管理接口（/user/, /role/）禁止 AUDIT_ADMIN 访问

### 权限隔离实现说明
- 所有返回的菜单数据需遵循前端动态路由所需结构：
- 树形结构，包含子节点列表
- 目录、菜单、按钮通过 menu_type 区分
- 按钮权限标识 perms 字段用于前端按钮级权限控制

### 核心组件位置
- system/src/main/java/com/xiyao/system 实体类相关文件在这个包下

```
system/src/main/java/com/xiyao/security/
├── config/
│   └── SecurityConfig.java           # Security 核心配置
├── controller/
│   ├── LoginController.java          # 登录控制器
│   ├── UserController.java           # 用户管理
│   ├── RoleController.java           # 角色管理
│   └── MenuController.java           # 菜单管理
├── details/
│   ├── LoginUser.java                # 登录用户信息
│   └── UserVo.java                    # 用户视图对象
├── enums/
│   └── AdminType.java                 # 管理员类型枚举
├── filter/
│   └── JwtAuthenticationFilter.java   # JWT 认证过滤器
├── handler/
│   ├── AccessDeniedHandlerImpl.java   # 无权限处理（返回JSON）
│   └── AuthenticationEntryPointImpl.java # 未登录处理（返回JSON）
├── properties/
│   └── SecurityData.java             # 安全配置属性
├── service/
│   └── impl/
│       └── UserDetailsServiceImpl.java  # 用户详情服务
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

```
-- 用户表
CREATE TABLE sys_user (
id          BIGINT PRIMARY KEY AUTO_INCREMENT,
username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '账号',
password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
real_name   VARCHAR(50)  COMMENT '真实姓名',
phone       VARCHAR(20)  COMMENT '手机号',
email       VARCHAR(100) COMMENT '邮箱',
status      TINYINT DEFAULT 0 COMMENT '状态 0正常 1停用',
del_flag    TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
create_by   VARCHAR(50)  COMMENT '创建者',
create_time DATETIME     COMMENT '创建时间',
update_by   VARCHAR(50)  COMMENT '更新者',
update_time DATETIME     COMMENT '更新时间',
remark      VARCHAR(500) COMMENT '备注'
) COMMENT '用户表';

-- 角色表
CREATE TABLE sys_role (
id          BIGINT PRIMARY KEY AUTO_INCREMENT,
role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
role_code   VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
role_sort   INT DEFAULT 0 COMMENT '排序',
status      TINYINT DEFAULT 0 COMMENT '状态 0正常 1停用',
del_flag    TINYINT DEFAULT 0 COMMENT '逻辑删除',
create_by   VARCHAR(50),
create_time DATETIME,
update_by   VARCHAR(50),
update_time DATETIME,
remark      VARCHAR(500)
) COMMENT '角色表';

-- 菜单表
CREATE TABLE sys_menu (
id          BIGINT PRIMARY KEY AUTO_INCREMENT,
parent_id   BIGINT DEFAULT 0 COMMENT '父菜单ID',
menu_name   VARCHAR(50) NOT NULL COMMENT '菜单名称',
menu_type   CHAR(1) DEFAULT 'M' COMMENT 'M目录 C菜单 F按钮',
path        VARCHAR(200) COMMENT '路由地址',
component   VARCHAR(255) COMMENT '组件路径',
perms       VARCHAR(100) COMMENT '权限标识',
icon        VARCHAR(100) COMMENT '图标',
sort        INT DEFAULT 0 COMMENT '排序',
status      TINYINT DEFAULT 0 COMMENT '状态',
del_flag    TINYINT DEFAULT 0 COMMENT '逻辑删除',
create_by   VARCHAR(50),
create_time DATETIME,
update_by   VARCHAR(50),
update_time DATETIME,
remark      VARCHAR(500)
) COMMENT '菜单表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
user_id BIGINT NOT NULL COMMENT '用户ID',
role_id BIGINT NOT NULL COMMENT '角色ID',
PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
role_id BIGINT NOT NULL COMMENT '角色ID',
menu_id BIGINT NOT NULL COMMENT '菜单ID',
PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联表';

```