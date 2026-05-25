package com.xiyao.security.utils;

import com.xiyao.security.details.LoginUser;
import com.xiyao.security.enums.AdminType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 * <p>
 * 提供获取当前登录用户信息的便捷方法，
 * 封装 Spring Security 的 SecurityContextHolder 操作。
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>在 Controller/Service/Mapper 中获取当前登录用户信息</li>
 *     <li>根据用户类型进行业务逻辑判断</li>
 *     <li>记录操作日志时获取当前用户 ID</li>
 * </ul>
 *
 * @author xiyao
 * @see SecurityContextHolder
 * @see LoginUser
 */
public class SecurityUtils {

    /**
     * 获取当前认证对象
     * <p>
     * 从 SecurityContextHolder 中获取当前的 Authentication 对象。
     *
     * @return 当前用户的认证对象，未认证则返回 null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 从 Authentication 对象中提取 LoginUser 主体信息。
     *
     * @return 当前登录用户的 LoginUser 对象，未登录则返回 null
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户的 ID
     *
     * @return 当前登录用户的 userId，未登录则返回 null
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return 当前登录用户的用户名，未登录则返回 null
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前用户的三员类型
     * <p>
     * 三员类型用于等保合规：
     * <ul>
     *     <li>0：普通用户</li>
     *     <li>1：系统管理员</li>
     *     <li>2：安全管理员</li>
     *     <li>3：审计管理员</li>
     * </ul>
     *
     * @return 三员类型枚举值，未登录则返回普通用户
     */
    public static Integer getAdminType() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getAdminType() : AdminType.NormalUser.ordinal();
    }

    /**
     * 判断是否是普通用户
     *
     * @return true 是普通用户，false 不是
     */
    public static boolean isNormalUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.NormalUser.ordinal();
    }

    /**
     * 判断是否是系统管理员
     *
     * @return true 是系统管理员，false 不是
     */
    public static boolean isSystemAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.SystemAdmin.ordinal();
    }

    /**
     * 判断是否是安全管理员
     *
     * @return true 是安全管理员，false 不是
     */
    public static boolean isSecurityAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.SecurityAdmin.ordinal();
    }

    /**
     * 判断是否是审计管理员
     *
     * @return true 是审计管理员，false 不是
     */
    public static boolean isAuditAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getAdminType() == AdminType.AuditAdmin.ordinal();
    }

    /**
     * 判断是否具有三员权限（系统管理员或安全管理员）
     * <p>
     * 用于需要三员权限才能访问的接口判断。
     *
     * @return true 具有三员权限，false 不具有
     */
    public static boolean hasAdminPermission() {
        Integer adminType = getAdminType();
        return adminType == AdminType.SystemAdmin.ordinal() || adminType == AdminType.SecurityAdmin.ordinal();
    }
}
