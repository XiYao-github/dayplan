package com.xiyao.security.details;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component("xiyao")
public class PermissionService {


    /**
     * 判断用户是否拥有某个权限
     *
     * @param permission 权限标识，如 "system:user:list"
     * @return true/false
     */
    public boolean hasPermission(String permission) {
        // 从 SecurityContext 获取当前用户的权限集合
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return false;
    }

    /**
     * 判断用户是否拥有任意一个权限
     *
     * @param permissions 权限列表
     * @return true/false
     */
    public boolean hasAnyPermission(String... permissions) {
        // 从 SecurityContext 获取当前用户的权限集合
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return false;
    }
}