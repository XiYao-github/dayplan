package com.xiyao.security.details;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xiyao.system.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户详情
 * <p>
 * 实现 Spring Security 的 UserDetails 接口，
 * 作为认证主体（Principal）存储在 SecurityContext 中。
 *
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>存储用户基本信息（ID、用户名、密码、三员类型）</li>
 *     <li>缓存用户权限集合用于权限校验</li>
 *     <li>实现 UserDetails 接口以适配 Spring Security 认证流程</li>
 * </ul>
 *
 * <p>
 * <b>序列化注意：</b>
 * 密码和权限信息使用 @JsonIgnore 注解，
 * 避免序列化到 Redis 时泄露敏感信息。
 *
 * @author xiyao
 * @see UserDetails
 * @see SysUser
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    /**
     * 用户 ID
     * <p>
     * 对应 sys_user 表的主键。
     */
    private Long userId;

    /**
     * 用户基本信息实体
     * <p>
     * 包含用户名、密码等敏感信息。
     */
    private SysUser sysUser;

    /**
     * 权限标识集合
     * <p>
     * 存储用户拥有的所有权限标识，
     * 格式如 ["system:user:list", "system:user:add"]。
     */
    private Set<String> permissions;

    /**
     * 获取权限列表
     * <p>
     * 将权限标识集合转换为 Spring Security 的 GrantedAuthority 集合，
     * 用于 @PreAuthorize 等注解的权限判断。
     *
     * @return 权限集合，永不为 null
     */
    @Override
    @JsonIgnore  // 序列化时忽略，避免权限列表暴露给前端
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptySet();
        }
        // 将权限标识集合转换为 GrantedAuthority 集合，供 Security 权限判断使用
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    /**
     * 获取密码
     * <p>
     * 用于 Spring Security 进行凭证校验。
     *
     * @return 用户密码（已加密）
     */
    @Override
    @JsonIgnore  // 序列化时忽略
    public String getPassword() {
        return ObjectUtil.isNotNull(sysUser) ? sysUser.getPassword() : null;
    }

    /**
     * 获取用户名
     * <p>
     * 用于 Spring Security 进行用户标识。
     *
     * @return 用户名
     */
    @Override
    @JsonIgnore  // 序列化时忽略
    public String getUsername() {
        return ObjectUtil.isNotNull(sysUser) ? sysUser.getUsername() : null;
    }
}