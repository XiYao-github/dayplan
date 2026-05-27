package com.xiyao.security.handler;

import com.xiyao.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 认证失败入口点处理器
 * <p>
 * 当用户未认证（如未登录）或 Token 无效时访问受保护资源，
 * 此处理器会被调用，返回统一的认证失败响应。
 *
 * <p>
 * <b>触发场景：</b>
 * <ul>
 *     <li>用户未登录，直接访问需要认证的接口</li>
 *     <li>用户携带的 JWT Token 已过期</li>
 *     <li>用户携带的 JWT Token 签名验证失败</li>
 *     <li>用户携带的 JWT Token 在 Redis 中已失效（被清除）</li>
 * </ul>
 *
 * <p>
 * <b>与 AccessDeniedHandlerImpl 的区别：</b>
 * <ul>
 *     <li>AuthenticationEntryPointImpl：用户未认证或认证失效（未登录）</li>
 *     <li>AccessDeniedHandlerImpl：用户已认证但权限不足</li>
 * </ul>
 *
 * <p>
 * <b>响应说明：</b>
 * <ul>
 *     <li>HTTP 状态码：200（业务响应码 401）</li>
 *     <li>Content-Type：application/json</li>
 *     <li>响应内容：统一 Result 格式的错误信息，提示重新登录</li>
 * </ul>
 *
 * @author xiyao
 * @see AccessDeniedHandlerImpl
 * @see AuthenticationException
 */
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * 处理认证失败异常
     * <p>
     * 当认证失败时，将异常信息包装为统一的 JSON 响应格式返回给客户端。
     *
     * @param request       HTTP 请求对象
     * @param response      HTTP 响应对象
     * @param authException 认证异常（包含失败原因）
     * @throws IOException 如果写入响应失败
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        WebUtils.print(response, HttpStatus.UNAUTHORIZED.value(), "认证失败,请重新登录.");
    }
}