package com.xiyao.security.details;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xiyao.system.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class LoginUser implements UserDetails {

    /**
     * 用户ID（业务用）
     */
    private Long userId;

    /**
     * 用户基本信息（原始对象）
     */
    private SysUser user;

    /**
     * 权限标识集合（如 ["system:user:list"]）
     */
    private Set<String> permissions;

    /**
     * Redis 中存储的 UUID 令牌标识（非 JWT 字符串）
     */
    private String token;

    /**
     * 登录时间戳
     */
    private Long loginTime;

    /**
     * 过期时间戳
     */
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
    @JsonIgnore // 序列化时忽略，避免权限列表暴露给前端
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


}