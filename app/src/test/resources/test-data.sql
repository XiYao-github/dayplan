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
VALUES (1, 'admin', '$2a$10$4HqrolfoG3Va1/MVmUoPjeocoZyv3ta/UbbkwPR5tFgQ9tJ2l5lvK', 1, 0),
       (2, 'user', '$2a$10$4HqrolfoG3Va1/MVmUoPjeocoZyv3ta/UbbkwPR5tFgQ9tJ2l5lvK', 1, 0);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2);