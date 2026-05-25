package com.xiyao.security.handler;

import cn.hutool.json.JSONUtil;
import com.xiyao.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 权限不足处理器
 * <p>
 * 当用户已认证但访问权限不足时（如访问需要更高权限的接口），
 * 此处理器会被调用，返回统一的权限不足响应。
 *
 * <p>
 * <b>触发场景：</b>
 * <ul>
 *     <li>用户已登录，但访问的接口需要更高角色权限</li>
 *     <li>用户已登录，但接口配置了 @PreAuthorize 权限校验失败</li>
 *     <li>用户已登录，但访问被安全策略拒绝</li>
 * </ul>
 *
 * <p>
 * <b>响应说明：</b>
 * <ul>
 *     <li>HTTP 状态码：200（业务响应码 403）</li>
 *     <li>Content-Type：application/json</li>
 *     <li>响应内容：统一 Result 格式的错误信息</li>
 * </ul>
 *
 * @author xiyao
 * @see AuthenticationEntryPointImpl
 * @see AccessDeniedException
 */
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * 处理权限不足异常
     * <p>
     * 将异常信息包装为统一的 JSON 响应格式返回给客户端。
     *
     * @param request                 HTTP 请求对象
     * @param response                HTTP 响应对象
     * @param accessDeniedException   权限不足异常（包含拒绝原因）
     * @throws IOException 如果写入响应失败
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // 设置 HTTP 状态码为 200（业务响应码在 Result 中返回 403）
        response.setStatus(HttpStatus.OK.value());

        // 设置响应内容类型为 JSON，字符编码为 UTF-8
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 构造统一的错误响应结果
        // HTTP 状态码 200，业务状态码 403，错误信息"权限不足，无法访问"
        Result error = Result.error(HttpStatus.FORBIDDEN.value(), "权限不足，无法访问.");

        // 将错误结果写入响应
        response.getWriter().write(JSONUtil.toJsonStr(error));
    }
}