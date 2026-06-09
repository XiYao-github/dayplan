package com.xiyao.security.service;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Security 权限服务
 * <p>
 * 提供 SpEL 表达式调用的权限校验方法，用于 @PreAuthorize 注解的 SpEL 表达式。
 * 封装 SecurityUtils 的核心方法，提供更便捷的权限判断功能。
 *
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>登录状态判断：isLogin()</li>
 *     <li>用户信息获取：getLoginUserId()、getLoginUser()</li>
 *     <li>角色权限判断：hasRole()、hasAnyRole()</li>
 *     <li>资源权限判断：hasAuthority()</li>
 * </ul>
 *
 * <p>
 * <b>使用方式：</b>
 * <pre>
 * &#64;PreAuthorize("@ss.isLogin()")  // 需要登录
 * &#64;PreAuthorize("@ss.hasRole('admin')")  // 具有 admin 角色
 * &#64;PreAuthorize("@ss.hasAnyRole('admin', 'user')")  // 任一角色
 * &#64;PreAuthorize("@ss.hasAuthority('system:user:delete')")  // 指定权限
 * </pre>
 *
 * @author xiyao
 * @see SecurityUtils
 */
@Service("ss")
public class SecurityService {

    /**
     * 判断是否已登录
     * <p>
     * 检查当前请求的用户是否已完成认证并登录。
     *
     * @return true 已登录，false 未登录
     */
    public boolean isLogin() {
        // 从 SecurityContext 获取登录用户，为空则未登录
        return ObjectUtil.isNotNull(SecurityUtils.getLoginUser());
    }

    /**
     * 获取当前登录用户 ID
     * <p>
     * 获取当前认证用户的唯一标识 ID。
     *
     * @return 当前登录用户的 ID，未登录返回 null
     */
    public Long getLoginUserId() {
        // 调用 SecurityUtils 获取用户 ID
        return SecurityUtils.getUserId();
    }

    /**
     * 获取当前登录用户
     * <p>
     * 获取包含完整信息的 LoginUser 对象。
     *
     * @return LoginUser 对象，未登录返回 null
     */
    public LoginUser getLoginUser() {
        // 调用 SecurityUtils 获取登录用户信息
        return SecurityUtils.getLoginUser();
    }

    /**
     * 判断是否具有指定角色权限
     * <p>
     * 检查当前用户是否拥有指定角色。
     * 角色名称不需要添加 ROLE_ 前缀，方法内部会自动添加。
     *
     * <p>
     * <b>示例：</b>
     * <pre>
     * // 检查用户是否有 admin 角色
     * ss.hasRole("admin")  // 内部转换为 ROLE_admin
     * </pre>
     *
     * @param role 角色名称（不含 ROLE_ 前缀）
     * @return true 表示具有该角色，false 表示不具有
     */
    public boolean hasRole(String role) {
        // 获取当前登录用户
        LoginUser user = SecurityUtils.getLoginUser();
        // 用户未登录，直接返回 false
        if (ObjectUtil.isNull(user)) {
            return false;
        }
        // 遍历用户权限列表，检查是否包含指定角色
        // Spring Security 角色权限格式为 "ROLE_" + 角色名
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 判断是否具有任一角色权限
     * <p>
     * 检查当前用户是否拥有指定角色列表中的任意一个角色。
     * 角色名称不需要添加 ROLE_ 前缀，方法内部会自动添加。
     *
     * <p>
     * <b>示例：</b>
     * <pre>
     * // 检查用户是否有 admin 或 super 角色之一
     * ss.hasAnyRole("admin", "super")
     * </pre>
     *
     * @param roles 角色名称数组（不含 ROLE_ 前缀）
     * @return true 表示具有任一角色，false 表示都不具有
     */
    public boolean hasAnyRole(String... roles) {
        // 获取当前登录用户
        LoginUser user = SecurityUtils.getLoginUser();
        // 用户未登录，直接返回 false
        if (ObjectUtil.isNull(user)) {
            return false;
        }
        // 将角色名称数组转换为带 ROLE_ 前缀的格式
        // 例如：["admin", "super"] -> ["ROLE_admin", "ROLE_super"]
        String[] roleAuthorities = Arrays.stream(roles)
                .map(r -> "ROLE_" + r)
                .toArray(String[]::new);
        // 遍历用户权限列表，检查是否包含任一指定角色
        return user.getAuthorities().stream()
                .anyMatch(a -> Arrays.asList(roleAuthorities).contains(a.getAuthority()));
    }

    /**
     * 判断是否具有指定权限
     * <p>
     * 检查当前用户是否拥有指定权限标识。
     * 权限标识格式通常为 "系统:模块:操作"，如 "system:user:delete"。
     *
     * <p>
     * <b>示例：</b>
     * <pre>
     * // 检查用户是否有删除用户的权限
     * ss.hasAuthority("system:user:delete")
     * </pre>
     *
     * @param authority 权限标识（如 system:user:delete）
     * @return true 表示具有该权限，false 表示不具有
     */
    public boolean hasAuthority(String authority) {
        // 获取当前登录用户
        LoginUser user = SecurityUtils.getLoginUser();
        // 用户未登录，直接返回 false
        if (ObjectUtil.isNull(user)) {
            return false;
        }
        // 遍历用户权限列表，检查是否包含指定权限标识
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}