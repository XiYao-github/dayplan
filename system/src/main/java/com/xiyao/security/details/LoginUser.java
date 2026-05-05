package com.xiyao.security.details;


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
 * 实现 UserDetails 接口，用于 Spring Security 认证和授权
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    /**
     * 用户基本信息（原始对象）
     */
    private Long userId;

    /**
     * 用户基本信息（原始对象）
     */
    private SysUser sysUser;

    /**
     * 权限标识集合（如 ["system:user:list"]）
     */
    private Set<String> permissions;

    /**
     * 获取权限列表（
     */
    @Override
    @JsonIgnore // 序列化时忽略，避免权限列表暴露给前端
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        // 将权限标识集合转换为 GrantedAuthority 集合，供 Security 权限判断使用
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return sysUser.getUsername();
    }

}