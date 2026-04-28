package com.xiyao.framework.security;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xiyao.system.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户身份权限对象（Spring Security 核心载体）
 *
 * 作用：
 *   1. 实现 UserDetails，使 Spring Security 能够识别用户身份
 *   2. 存储用户基本信息、权限标识集合、Redis token 唯一标识
 *   3. 提供 getAuthorities() 方法，将权限标识集合转换为 GrantedAuthority 列表
 *
 * 调用链：
 *   - UserDetailsServiceImpl.loadUserByUsername() 返回 LoginUser
 *   - TokenService 将其存入 Redis
 *   - JwtAuthenticationTokenFilter 从 Redis 取出并放入 SecurityContext
 *   - PermissionService 从 SecurityContext 中获取 LoginUser 并判断权限
 */
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户ID（业务用） */
    private Long userId;

    /** 用户基本信息（原始对象） */
    private SysUser user;

    /** 权限标识集合（如 ["system:user:list"]） */
    private Set<String> permissions;

    /** Redis 中存储的 UUID 令牌标识（非 JWT 字符串） */
    private String token;

    /** 登录时间戳 */
    private Long loginTime;

    /** 过期时间戳 */
    private Long expireTime;

    // 构造器
    public LoginUser(SysUser user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
        this.userId = user.getId();
    }

    /**
     * 获取权限列表（Spring Security 调用）
     * 将 Set<String> 权限标识转换为 GrantedAuthority 列表
     * 这样后续 @PreAuthorize("hasAuthority('system:user:list')") 才能生效
     */
    @Override
    @JsonIgnore  // 序列化时忽略，避免权限列表暴露给前端
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) {
            return java.util.Collections.emptyList();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        // "0" 表示正常，"1" 表示停用
        return "0".equals(user.getStatus());
    }

    // ----- getter / setter -----
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public SysUser getUser() { return user; }
    public void setUser(SysUser user) { this.user = user; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getLoginTime() { return loginTime; }
    public void setLoginTime(Long loginTime) { this.loginTime = loginTime; }
    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
}