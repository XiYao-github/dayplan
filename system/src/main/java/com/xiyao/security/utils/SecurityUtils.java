package com.xiyao.security.utils;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.security.details.LoginUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 安全服务工具类
 * <p>
 * 提供获取当前登录用户信息的便捷方法，封装 Spring Security 的 SecurityContextHolder 操作。
 * 用于在 Service、Controller 等层面获取当前认证用户信息。
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>获取当前登录用户 ID，记录操作日志</li>
 *     <li>根据用户类型进行业务逻辑判断</li>
 *     <li>权限校验辅助判断</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * // 获取用户 ID
 * Long userId = SecurityUtils.getUserId();
 *
 * // 判断是否系统管理员
 * if (SecurityUtils.isSystemAdmin()) {
 *     // 系统管理员逻辑
 * }
 * </pre>
 *
 * @author xiyao
 * @see SecurityContextHolder
 * @see LoginUser
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {

    // ==================== 认证信息获取 ====================

    /**
     * 获取当前认证对象
     * <p>
     * 从 SecurityContextHolder 中获取当前的 Authentication 对象。
     * Authentication 对象包含认证主体、凭证和权限信息。
     *
     * @return 当前用户的 Authentication 对象，未认证则返回 null
     */
    public static Authentication getAuthentication() {
        // 从 SecurityContext 获取当前认证信息
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 从 Authentication 对象中提取 LoginUser 主体信息。
     * LoginUser 包含用户 ID、三员类型、权限集合等。
     *
     * @return 当前登录用户的 LoginUser 对象，未登录则返回 null
     */
    public static LoginUser getLoginUser() {
        // 获取当前认证对象
        Authentication authentication = getAuthentication();
        // 检查认证对象是否存在且主体为 LoginUser 类型
        if (ObjectUtil.isNotNull(authentication) && authentication.getPrincipal() instanceof LoginUser) {
            // 强制类型转换并返回
            return (LoginUser) authentication.getPrincipal();
        }
        // 未登录或类型不匹配，返回 null
        return null;
    }

    // ==================== 用户基本信息获取 ====================

    /**
     * 获取当前登录用户的 ID
     * <p>
     * 用于记录操作日志、关联业务数据等场景。
     *
     * @return 当前登录用户的 userId，未登录则返回 null
     */
    public static Long getUserId() {
        // 获取登录用户对象
        LoginUser loginUser = getLoginUser();
        // 三元表达式：登录返回 userId，未登录返回 null
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前登录用户的用户名
     * <p>
     * 用于显示、记录等场景。
     *
     * @return 当前登录用户的用户名，未登录则返回 null
     */
    public static String getUsername() {
        // 获取登录用户对象
        LoginUser loginUser = getLoginUser();
        // 三元表达式：登录返回用户名，未登录返回 null
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getUsername() : null;
    }
}