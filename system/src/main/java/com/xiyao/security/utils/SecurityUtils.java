package com.xiyao.security.utils;

import com.xiyao.security.details.LoginUser;
import com.xiyao.security.enums.AdminType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    /**
     * 获取Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户信息
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前用户的三员类型
     *
     * @return 0普通用户 1系统管理员 2安全管理员 3审计管理员
     */
    public static Integer getAdminType() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getAdminType() : AdminType.NormalUser.ordinal();
    }

    /**
     * 判断是否是普通用户
     */
    public static boolean isNormalUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.NormalUser.ordinal();
    }

    /**
     * 判断是否是系统管理员
     */
    public static boolean isSystemAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.SystemAdmin.ordinal();
    }

    /**
     * 判断是否是安全管理员
     */
    public static boolean isSecurityAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.SecurityAdmin.ordinal();
    }

    /**
     * 判断是否是审计管理员
     */
    public static boolean isAuditAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.AuditAdmin.ordinal();
    }

    /**
     * 判断是否具有三员权限（系统管理员或安全管理员）
     */
    public static boolean hasAdminPermission() {
        Integer adminType = getAdminType();
        return adminType == AdminType.SystemAdmin.ordinal() || adminType == AdminType.SecurityAdmin.ordinal();
    }

}
