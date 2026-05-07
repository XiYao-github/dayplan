package com.xiyao.framework.utils;


import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 请求上下文工具类
 * <p>
 * 提供获取当前请求的 HttpServletRequest、客户端 IP、User-Agent 等信息。
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
public class RequestUtils {

    /**
     * 获取当前请求的 HttpServletRequest
     *
     * @return HttpServletRequest，如果不在 Web 上下文中则返回 null
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 支持 Nginx 反向代理，优先从 X-Forwarded-For 头中获取。
     * </p>
     *
     * @return IP 地址，无法获取时返回 "unknown"
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理的情况，取第一个非 unknown 的 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取请求方法（GET、POST 等）
     */
    public static String getMethod() {
        HttpServletRequest request = getRequest();
        return request == null ? "unknown" : request.getMethod();
    }

    /**
     * 获取请求 URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        return request == null ? "unknown" : request.getRequestURI();
    }

    /**
     * 获取 User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return request == null ? "unknown" : request.getHeader("User-Agent");
    }

    /**
     * 判断是否为 AJAX 请求
     */
    public static boolean isAjaxRequest() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }
}