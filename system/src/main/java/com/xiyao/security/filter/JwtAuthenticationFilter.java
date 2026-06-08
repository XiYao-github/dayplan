package com.xiyao.security.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 职责：
 * <ul>
 *     <li>从请求 Header/参数中提取 JWT Token</li>
 *     <li>验证 Token 有效性（未过期、签名正确）</li>
 *     <li>解析用户信息，设置 SecurityContext 认证上下文</li>
 * </ul>
 *
 * <p>
 * <b>认证流程：</b>
 * <ol>
 *     <li>请求进入此过滤器</li>
 *     <li>从 Header/参数中获取 Token（通过 JwtUtils）</li>
 *     <li>验证 Token 有效性</li>
 *     <li>解析用户信息，构造 Authentication 对象</li>
 *     <li>存入 SecurityContextHolder，后续可通过 SecurityContext 获取用户</li>
 * </ol>
 *
 * <p>
 * <b>放置位置：</b>
 * 在 UsernamePasswordAuthenticationFilter 之前执行，
 * 确保在登录认证之前就已设置好用户上下文。
 *
 * @author xiyao
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT 工具类
     */
    private final JwtUtils jwtUtils;

    /**
     * 过滤器核心方法
     * <p>
     * 每次请求都会执行此方法进行 Token 认证。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求中获取 Token
        String token = jwtUtils.getHeaderToken(request);

        // Token 存在且有效
        if (StrUtil.isNotBlank(token) && jwtUtils.validateToken(token)) {
            // 解析用户信息
            LoginUser loginUser = jwtUtils.getLoginUser(token);

            if (ObjectUtil.isNotEmpty(loginUser)) {
                // 构造已认证的 Authentication 对象
                // 参数：principal（用户信息）、credentials（凭证，通常为null）、authorities（权限）
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

                // 认证通过，将用户信息存入 SecurityContext
                // 后续可通过 SecurityContextHolder.getContext().getAuthentication() 获取当前用户
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Token 有效但用户缓存已失效（如被踢出登录），清除上下文
                SecurityContextHolder.clearContext();
            }
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}