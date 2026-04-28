package com.xiyao.framework.security;


import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 自定义权限注解服务（Bean 名称为 "ss"）
 * 配合 @PreAuthorize("@ss.hasPermi('xxx')") 使用
 *
 * 为什么不用 Spring 原生 hasAuthority？
 *   若依风格权限标识为字符串如 "system:user:list"，原生 hasAuthority 也可用，
 *   但 PermissionService 可以提供更灵活的扩展（如逻辑或、超级管理员自动放行等）。
 */
@Service("ss")   // 注意 Bean 名称 "ss"，在 SpEL 中通过 @ss.hasPermi 调用
public class PermissionService {

    /**
     * 判断用户是否拥有某个权限
     * @param permission 权限标识，如 "system:user:list"
     * @return true/false
     */
    public boolean hasPermi(String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        Set<String> permissions = loginUser.getPermissions();
        // 超级管理员直接放行
        if (permissions.contains(JwtConstants.ALL_PERMISSION)) {
            return true;
        }
        return permissions.contains(permission);
    }

    /**
     * 判断用户是否拥有任意一个权限
     * @param permissions 权限列表
     * @return true/false
     */
    public boolean hasAnyPermi(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        Set<String> userPerms = loginUser.getPermissions();
        if (userPerms.contains(JwtConstants.ALL_PERMISSION)) {
            return true;
        }
        for (String perm : permissions) {
            if (userPerms.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    // 也可添加 hasRole、lacksPermi 等方法，原理相同
}