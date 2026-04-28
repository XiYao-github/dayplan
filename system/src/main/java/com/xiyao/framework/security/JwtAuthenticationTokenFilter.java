package com.xiyao.framework.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器（每个请求都会经过）
 *
 * 作用：
 *   1. 解析请求头中的 JWT
 *   2. 通过 TokenService 从 Redis 获取 LoginUser
 *   3. 将 LoginUser 包装为 Authentication 对象，存入 SecurityContextHolder
 *   4. 使后续的权限判断（@PreAuthorize）能够获取到当前用户信息
 *
 * 配置位置：在 SecurityConfig 中通过 addFilterBefore 添加到过滤器链
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 1. 获取当前登录用户
        LoginUser loginUser = tokenService.getLoginUser(request);
        // 2. 如果用户存在且尚未认证（避免重复设置）
        if (loginUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 3. 创建 Authentication 对象
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 4. 存入 SecurityContext（这样 Spring Security 就知道当前用户是谁了）
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        // 5. 放行
        chain.doFilter(request, response);
    }
}