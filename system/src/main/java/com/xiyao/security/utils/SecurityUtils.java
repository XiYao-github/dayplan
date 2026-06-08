package com.xiyao.security.utils;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.enums.AdminType;
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

    /**
     * 获取当前用户的三员类型
     * <p>
     * 三员类型用于等保合规的三员管理机制。
     *
     * <p>
     * <b>三员类型定义：</b>
     * <ul>
     *     <li>0 - NormalUser（普通用户）</li>
     *     <li>1 - SystemAdmin（系统管理员）</li>
     *     <li>2 - SecurityAdmin（安全管理员）</li>
     *     <li>3 - AuditAdmin（审计管理员）</li>
     * </ul>
     *
     * @return 三员类型枚举值，未登录则返回普通用户（0）
     */
    public static Integer getAdminType() {
        // 获取登录用户对象
        LoginUser loginUser = getLoginUser();
        // 三元表达式：登录返回 adminType，未登录返回普通用户类型
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getAdminType() : AdminType.NormalUser.ordinal();
    }

    // ==================== 三员类型判断 ====================

    /**
     * 判断是否是普通用户
     * <p>
     * 普通用户的三员类型为 0，无特殊权限。
     *
     * @return true 是普通用户，false 不是（包括未登录）
     */
    public static boolean isNormalUser() {
        // 获取登录用户
        LoginUser loginUser = getLoginUser();
        // 判断条件：用户存在且三员类型为普通用户
        return ObjectUtil.isNotNull(loginUser) && loginUser.getAdminType() == AdminType.NormalUser.ordinal();
    }

    /**
     * 判断是否是系统管理员
     * <p>
     * 系统管理员职责：
     * <ul>
     *     <li>负责系统配置、用户账号管理</li>
     *     <li>可以创建用户并指定其为安全保密管理员</li>
     *     <li>不能操作业务数据，不能查看安全审计日志</li>
     * </ul>
     *
     * @return true 是系统管理员，false 不是
     */
    public static boolean isSystemAdmin() {
        // 获取登录用户
        LoginUser loginUser = getLoginUser();
        // 判断条件：用户存在且三员类型为系统管理员
        return ObjectUtil.isNotNull(loginUser) && loginUser.getAdminType() == AdminType.SystemAdmin.ordinal();
    }

    /**
     * 判断是否是安全管理员
     * <p>
     * 安全管理员职责：
     * <ul>
     *     <li>负责用户权限分配、安全策略设置</li>
     *     <li>可以分配菜单权限、设置密码策略、配置会话超时时间</li>
     *     <li>不能查看审计日志，无法知道自己被审计管理员监督的情况</li>
     * </ul>
     *
     * @return true 是安全管理员，false 不是
     */
    public static boolean isSecurityAdmin() {
        // 获取登录用户
        LoginUser loginUser = getLoginUser();
        // 判断条件：用户存在且三员类型为安全管理员
        return ObjectUtil.isNotNull(loginUser) && loginUser.getAdminType() == AdminType.SecurityAdmin.ordinal();
    }

    /**
     * 判断是否是审计管理员
     * <p>
     * 审计管理员职责：
     * <ul>
     *     <li>负责查看和导出审计日志，监督其他两员操作</li>
     *     <li>拥有日志完整查看权限</li>
     *     <li>没有任何系统配置权限和权限分配权限</li>
     * </ul>
     *
     * @return true 是审计管理员，false 不是
     */
    public static boolean isAuditAdmin() {
        // 获取登录用户
        LoginUser loginUser = getLoginUser();
        // 判断条件：用户存在且三员类型为审计管理员
        return ObjectUtil.isNotNull(loginUser) && loginUser.getAdminType() == AdminType.AuditAdmin.ordinal();
    }

    /**
     * 判断是否具有三员权限
     * <p>
     * 判断用户是否为系统管理员或安全管理员。
     * 审计管理员不属于三员权限范畴，单独管理。
     *
     * <p>
     * <b>三员权限说明：</b>
     * 系统管理员和安全管理员具有三员权限，可以进行系统配置和权限管理操作。
     *
     * @return true 具有三员权限，false 不具有
     */
    public static boolean hasAdminPermission() {
        // 获取当前用户三员类型
        Integer adminType = getAdminType();
        // 判断是否为系统管理员或安全管理员
        return adminType == AdminType.SystemAdmin.ordinal()
                || adminType == AdminType.SecurityAdmin.ordinal();
    }
}