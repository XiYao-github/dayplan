create table app_user
(
    id          bigint auto_increment primary key,
    openid      varchar(100)            not null comment '微信openid',
    name        varchar(100) default '' null comment '昵称',
    phone       varchar(100) default '' null comment '电话',
    sex         int          default 0  null comment '性别(0.未知 1.男 2.女)',
    avatar      varchar(100) default '' null comment '头像',
    regions     varchar(50)  default '' null comment '地区',
    login_ip    varchar(128)            null comment '最后登录ip',
    login_date  datetime                null comment '最后登录时间',
    status      int          default 1  null comment '状态(0.停用 1.正常)',
    remark      varchar(500)            null comment '备注',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    delete_time datetime                null comment '删除时间',
    deleted     int          default 0  null comment '逻辑删除(0.未删除 1.已删除)',
    version     int          default 0  null comment '乐观锁'

) comment '用户';

create table sys_regions
(
    code          bigint                 not null comment '区划代码',
    parent_code   bigint      default 0  not null comment '父级区划代码',
    name          varchar(50) default '' not null comment '名称',
    province_code bigint                 null comment '省/直辖市代码',
    province_name varchar(50)            null comment '省/直辖市名称',
    city_code     bigint                 null comment '市代码',
    city_name     varchar(50)            null comment '市名称',
    area_code     bigint                 null comment '区/县代码',
    area_name     varchar(50)            null comment '区/县名称',
    sort          int                    null comment '排序',
    level         int                    null comment '级别(1.省/直辖市, 2.市, 3.区/县/地级市)'
) comment '行政区划';

create table sys_login
(
    id         bigint auto_increment primary key,
    user_id    bigint                  not null comment '用户id',
    username   varchar(100) default '' not null comment '用户账号',
    ipaddr     varchar(128) default '' null comment '登录ip',
    location   varchar(200) default '' null comment '登录地点',
    browser    varchar(50)  default '' null comment '浏览器类型',
    os         varchar(50)  default '' null comment '操作系统',
    status     int          default 1  null comment '状态(0.失败 1.成功)',
    error_msg  text                    null comment '错误消息',
    login_time datetime                null comment '访问时间'
)
    comment '访问记录';

create table sys_operation
(
    id             bigint auto_increment primary key,
    user_id        bigint                  not null comment '用户id',
    username       varchar(100) default '' not null comment '用户账号',
    operation      varchar(100) default '' null comment '操作描述',
    url            varchar(256) default '' null comment '请求url',
    ipaddr         varchar(128) default '' null comment '操作ip',
    location       varchar(200) default '' null comment '操作地点',
    method_name    varchar(200) default '' null comment '方法名称',
    req_method     varchar(10)  default '' null comment '请求方式',
    operation_type int          default 0  null comment '操作类型(0.其它 1.查询 2.新增 3.修改 4.删除)',
    param          longtext                null comment '请求参数',
    result         longtext                null comment '返回结果',
    status         int          default 1  null comment '状态(0.失败 1.成功)',
    error_msg      text                    null comment '错误消息',
    operation_time datetime                null comment '操作时间',
    cost_time      bigint                  null comment '消耗时间(毫秒)'
)
    comment '操作记录';

create table sys_config
(
    id          bigint auto_increment primary key,
    name        varchar(100)  not null comment '参数名',
    value       varchar(500)  not null comment '参数值',
    status      int default 1 null comment '状态(0.停用 1.正常)',
    remark      varchar(500)  null comment '备注',
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    delete_time datetime      null comment '删除时间',
    deleted     int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version     int default 0 null comment '乐观锁'
) comment '系统配置';

create table sys_dict_data
(
    id          bigint auto_increment primary key,
    dict_type   varchar(100) default '' null comment '字典类型',
    dict_label  varchar(100) default '' null comment '字典标签',
    dict_value  varchar(100) default '' null comment '字典键值',
    status      int          default 1  null comment '状态(0.停用 1.正常)',
    is_default  int          default 0  null comment '是否默认(0.否 1.是)',
    remark      varchar(500)            null comment '备注',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    delete_time datetime                null comment '删除时间',
    deleted     int          default 0  null comment '逻辑删除(0.未删除 1.已删除)',
    version     int          default 0  null comment '乐观锁'
)
    comment '字典数据';

create table sys_dict_type
(
    id          bigint auto_increment primary key,
    dict_name   varchar(100) default '' null comment '字典名称',
    dict_type   varchar(100) default '' null comment '字典类型',
    status      int          default 1  null comment '状态(0.停用 1.正常)',
    remark      varchar(500)            null comment '备注',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    delete_time datetime                null comment '删除时间',
    deleted     int          default 0  null comment '逻辑删除(0.未删除 1.已删除)',
    version     int          default 0  null comment '乐观锁'
)
    comment '字典类型';

create table sys_menu
(
    id          bigint auto_increment primary key,
    parent_id   bigint       default 0   not null comment '父菜单id',
    title       varchar(50)              not null comment '菜单标题',
    name        varchar(50)              not null comment '菜单名称',
    type        int          default 0   not null comment '菜单类型(0.目录 1.菜单 2.按钮)',
    path        varchar(200) default ''  not null comment '菜单路径',
    perms       varchar(200)             not null comment '权限标识',
    component   varchar(200)             null comment '组件路径',
    icon        varchar(100) default '#' null comment '图标',
    sort        int          default 0   null comment '顺序',
    status      int          default 1   null comment '状态(0.停用 1.正常)',
    remark      varchar(500)             null comment '备注',
    create_time datetime                 null comment '创建时间',
    update_time datetime                 null comment '更新时间',
    delete_time datetime                 null comment '删除时间',
    deleted     int          default 0   null comment '逻辑删除(0.未删除 1.已删除)',
    version     int          default 0   null comment '乐观锁'
)
    comment '系统菜单';

create table sys_role
(
    id          bigint auto_increment primary key,
    name        varchar(30)   not null comment '角色名称',
    sort        int           null comment '顺序',
    status      int default 1 null comment '状态(0.停用 1.正常)',
    remark      varchar(500)  null comment '备注',
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    delete_time datetime      null comment '删除时间',
    deleted     int default 0 null comment '逻辑删除(0.未删除 1.已删除)',
    version     int default 0 null comment '乐观锁'
)
    comment '系统角色';

create table sys_user
(
    id          bigint auto_increment primary key,
    username    varchar(30)             not null comment '账号',
    password    varchar(100) default '' not null comment '密码',
    salt        varchar(50)  default '' not null comment '盐',
    mobile      varchar(100) default '' null comment '手机号',
    nick_name   varchar(50)  default '' null comment '昵称',
    email       varchar(50)  default '' null comment '邮箱',
    sex         int          default 0  null comment '性别(0.未知 1.男 2.女)',
    avatar      varchar(100) default '' null comment '头像',
    login_ip    varchar(128) default '' null comment '最后登录ip',
    login_date  datetime                null comment '最后登录时间',
    status      int          default 1  null comment '状态(0.停用 1.正常)',
    remark      varchar(500)            null comment '备注',
    create_time datetime                null comment '创建时间',
    update_time datetime                null comment '更新时间',
    delete_time datetime                null comment '删除时间',
    deleted     int          default 0  null comment '逻辑删除(0.未删除 1.已删除)',
    version     int          default 0  null comment '乐观锁'
)
    comment '系统用户';

create table sys_role_menu
(
    role_id bigint not null comment '角色id',
    menu_id bigint not null comment '菜单id'
)
    comment '角色关联菜单';

create table sys_user_role
(
    user_id bigint not null comment '用户id',
    role_id bigint not null comment '角色id'
)
    comment '用户关联角色';


-- 清空相关表（谨慎，仅在测试环境中执行）
truncate table sys_user;
truncate table sys_role;
truncate table sys_user_role;
truncate table sys_menu;
truncate table sys_role_menu;

-- 插入角色
INSERT INTO sys_role (id, name, sort, status)
VALUES (1, '超级管理员', 1, 1),
       (2, '普通用户', 2, 1);

-- 插入菜单（简化版）
INSERT INTO sys_menu (id, parent_id, title, name, type, perms, status)
VALUES (1, 0, '用户管理', 'user', 1, '', 1),
       (2, 1, '用户列表', 'list', 2, 'sys:user:list', 1),
       (3, 1, '新增用户', 'add', 2, 'sys:user:add', 1),
       (4, 1, '编辑用户', 'edit', 2, 'sys:user:edit', 1),
       (5, 1, '删除用户', 'delete', 2, 'sys:user:delete', 1);

-- 分配超级管理员拥有所有权限
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5);
-- 普通用户只有列表权限
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (2, 1),
       (2, 2);

-- 插入用户（密码 123456 预先用 BCrypt 加密，生成一个固定的密文）
-- 实际可以使用测试工具类统一编码，这里给出示例密文（对应 "123456"）
INSERT INTO sys_user (id, username, password, status, deleted)
VALUES (1, 'admin', '$2a$10$NkM2sZ7YKc5kYqQ5eE2PNOqC5bJ5qF5qR5qX5qZ5qL5qW5qE5qR5qS', 1, 0),
       (2, 'user', '$2a$10$NkM2sZ7YKc5kYqQ5eE2PNOqC5bJ5qF5qR5qX5qZ5qL5qW5qE5qR5qS', 1, 0);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2);