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
 *     <li>三员类型判断：isSystemAdmin()、isSecurityAdmin()、isAuditAdmin()、isNormalUser()</li>
 *     <li>角色权限判断：hasRole()、hasAnyRole()</li>
 *     <li>资源权限判断：hasAuthority()</li>
 * </ul>
 *
 * <p>
 * <b>使用方式：</b>
 * <pre>
 * &#64;PreAuthorize("@ss.isLogin()")  // 需要登录
 * &#64;PreAuthorize("@ss.isSystemAdmin()")  // 系统管理员
 * &#64;PreAuthorize("@ss.hasAnyRole('admin', 'user')")  // 任一角色
 * &#64;PreAuthorize("@ss.hasAuthority('system:user:delete')")  // 指定权限
 * </pre>
 *
 * <p>
 * <b>三员类型说明：</b>
 * <ul>
 *     <li>系统管理员(1)：负责系统配置、用户账号管理</li>
 *     <li>安全管理员(2)：负责用户权限分配、安全策略设置</li>
 *     <li>审计管理员(3)：负责查看和导出审计日志，监督其他两员操作</li>
 *     <li>普通用户(0)：无特殊权限的普通用户</li>
 * </ul>
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
     * 判断是否是普通用户
     * <p>
     * 普通用户的三员类型为 0，不具备三员权限。
     *
     * @return true 是普通用户，false 不是
     */
    public boolean isNormalUser() {
        // 调用 SecurityUtils 判断普通用户
        return SecurityUtils.isNormalUser();
    }

    /**
     * 判断是否是系统管理员
     * <p>
     * 系统管理员负责系统配置、用户账号管理。
     * 不能操作业务数据，不能查看安全审计日志。
     *
     * @return true 是系统管理员，false 不是
     */
    public boolean isSystemAdmin() {
        // 调用 SecurityUtils 判断系统管理员
        return SecurityUtils.isSystemAdmin();
    }

    /**
     * 判断是否是安全管理员
     * <p>
     * 安全管理员负责用户权限分配、安全策略设置。
     * 不能查看审计日志，无法知道自己被审计管理员监督的情况。
     *
     * @return true 是安全管理员，false 不是
     */
    public boolean isSecurityAdmin() {
        // 调用 SecurityUtils 判断安全管理员
        return SecurityUtils.isSecurityAdmin();
    }

    /**
     * 判断是否是审计管理员
     * <p>
     * 审计管理员负责查看和导出审计日志，监督其他两员操作。
     * 拥有日志完整查看权限，但没有系统配置权限和权限分配权限。
     *
     * @return true 是审计管理员，false 不是
     */
    public boolean isAuditAdmin() {
        // 调用 SecurityUtils 判断审计管理员
        return SecurityUtils.isAuditAdmin();
    }

    /**
     * 判断是否具有三员权限
     * <p>
     * 判断用户是否为系统管理员或安全管理员。
     * 审计管理员不属于三员权限范畴。
     *
     * @return true 具有三员权限，false 不具有
     */
    public boolean hasAnyAdmin() {
        // 调用 SecurityUtils 判断三员权限
        return SecurityUtils.hasAdminPermission();
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