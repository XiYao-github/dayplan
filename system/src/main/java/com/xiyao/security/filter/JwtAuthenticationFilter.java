package com.xiyao.security.filter;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtils.getHeaderToken(request);
        if (StrUtil.isNotBlank(token) && jwtUtils.validateToken(token)) {
            // 解析用户账号
            LoginUser loginUser = jwtUtils.getLoginUser(token);
            if (ObjectUtil.isNotEmpty(loginUser)) {
                // 构造已认证的 Authentication 对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                // 认证通过，将认证信息存入上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 缓存已失效，重新登录
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}