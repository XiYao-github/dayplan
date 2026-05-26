package com.xiyao.security.service;

import com.xiyao.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Security 权限服务
 * <p>
 * 提供 SpEL 表达式调用权限校验方法，
 * 用于 @PreAuthorize 注解的 SpEL 表达式。
 *
 * <p>
 * 使用方式：
 * <pre>
 * &#64;PreAuthorize("@ss.isSystemAdmin()")
 * &#64;PreAuthorize("@ss.hasAnyAdmin()")
 * </pre>
 *
 * @author xiyao
 */
@Slf4j
@Service("ss")
public class SecurityService {

    /**
     * 判断是否是系统管理员
     */
    public boolean isSystemAdmin() {
        return SecurityUtils.isSystemAdmin();
    }

    /**
     * 判断是否是安全管理员
     */
    public boolean isSecurityAdmin() {
        return SecurityUtils.isSecurityAdmin();
    }

    /**
     * 判断是否是审计管理员
     */
    public boolean isAuditAdmin() {
        return SecurityUtils.isAuditAdmin();
    }

    /**
     * 判断是否具有三员权限（系统管理员或安全管理员）
     */
    public boolean hasAnyAdmin() {
        return SecurityUtils.hasAdminPermission();
    }

    /**
     * 判断是否具有指定角色权限
     */
    public boolean hasRole(String role) {
        return SecurityUtils.hasAdminPermission();
    }
}