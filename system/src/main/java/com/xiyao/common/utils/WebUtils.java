package com.xiyao.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web 工具类（前后端分离版）
 * <p>
 * 针对前后端分离架构，主要提供：
 * - 请求/响应对象获取
 * - 请求参数获取
 * - 请求体 JSON 读取
 * - Token/Header 获取
 * - 客户端 IP 获取
 * - JSON 响应输出
 * <p>
 * 不包含 Session、Cookie、重定向等传统 Web 相关方法
 *
 * @author xiyao
 */
public final class WebUtils {

    private WebUtils() {
        // 私有构造方法，防止实例化
    }

    // ==================== 获取请求/响应对象 ====================

    /**
     * 获取当前请求的 HttpServletRequest 对象
     * <p>
     * 只能在 Web 请求线程中调用（Controller、Interceptor、Filter 中有效）
     *
     * @return HttpServletRequest 对象，如果不在 Web 请求上下文中则返回 null
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }

    /**
     * 获取当前请求的 HttpServletResponse 对象
     *
     * @return HttpServletResponse 对象，如果不在 Web 请求上下文中则返回 null
     */
    public static HttpServletResponse getResponse() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getResponse();
        }
        return null;
    }

    /**
     * 判断当前线程是否处于 Web 请求上下文中
     *
     * @return true 表示当前处于 Web 请求上下文中
     */
    public static boolean isWebRequest() {
        return RequestContextHolder.getRequestAttributes() != null;
    }

    // ==================== 请求基本信息 ====================

    /**
     * 获取请求方式（GET、POST、PUT、DELETE 等）
     */
    public static String getMethod() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getMethod() : null;
    }

    /**
     * 获取请求 URI（不包含域名和端口）
     * <p>
     * 例如：/api/user/1
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取完整请求 URL（包含查询参数）
     * <p>
     * 例如：http://localhost:8080/api/user?name=zhangsan
     */
    public static String getFullRequestUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    // ==================== 获取请求头（Token 相关） ====================

    /**
     * 获取请求头信息
     *
     * @param name Header 名称（如 Authorization、Content-Type）
     * @return Header 值，不存在返回 null
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * 获取 Authorization 头（用于 JWT Token）
     * <p>
     * 通常格式：Bearer xxxxx
     *
     * @return Authorization 头值
     */
    public static String getAuthorization() {
        return getHeader("Authorization");
    }

    /**
     * 获取 Bearer Token
     * <p>
     * 从 Authorization 头中提取 token，去掉 "Bearer " 前缀
     *
     * @return token 字符串，不存在或格式不对返回 null
     */
    public static String getBearerToken() {
        String auth = getAuthorization();
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    /**
     * 获取 Content-Type 请求头
     */
    public static String getContentType() {
        return getHeader("Content-Type");
    }

    /**
     * 判断是否为 JSON 请求
     * <p>
     * 检查 Content-Type 是否包含 application/json
     */
    public static boolean isJsonRequest() {
        String contentType = getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * 获取所有请求头信息
     */
    public static Map<String, String> getHeaders() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    // ==================== 获取客户端信息 ====================

    /**
     * 获取客户端 IP 地址
     * <p>
     * 考虑 Nginx 反向代理，依次从以下 Header 中获取：
     * X-Forwarded-For -> X-Real-IP -> request.getRemoteAddr()
     *
     * @return 客户端 IP 地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况下，取第一个真实 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取 User-Agent（客户端类型识别）
     */
    public static String getUserAgent() {
        return getHeader("User-Agent");
    }

    // ==================== 获取请求参数（URL Query 参数） ====================

    /**
     * 获取请求参数值（URL 中的 Query 参数）
     *
     * @param name 参数名
     * @return 参数值，不存在返回 null
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * 获取请求参数值（带默认值）
     */
    public static String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * 获取请求参数并转换为 Integer
     */
    public static Integer getParameterInt(String name) {
        String value = getParameter(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取请求参数并转换为 Long
     */
    public static Long getParameterLong(String name) {
        String value = getParameter(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取所有请求参数（URL Query 参数）
     */
    public static Map<String, String> getParameters() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Collections.emptyMap();
        }
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                result.put(entry.getKey(), values[0]);
            }
        }
        return result;
    }

    // ==================== 获取请求体（JSON 数据） ====================

    /**
     * 获取请求体内容（适用于 POST、PUT 等请求的 JSON 数据）
     * <p>
     * 注意：请求体只能读取一次，如果在 Filter 或 Interceptor 中读取过，
     * 后续 Controller 将无法读取。如需重复读取，请使用 getCachingRequestWrapper 包装。
     *
     * @return 请求体字符串
     */
    public static String getRequestBody() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            return null;
        }
    }

    // ==================== AJAX 请求判断 ====================

    /**
     * 判断是否为 AJAX 请求
     * <p>
     * 前后端分离项目中，前端通常会带 X-Requested-With 头
     */
    public static boolean isAjaxRequest() {
        return "XMLHttpRequest".equalsIgnoreCase(getHeader("X-Requested-With"));
    }

    // ==================== 响应输出 ====================

    /**
     * 输出 JSON 响应
     * <p>
     * 在 Filter 或 Interceptor 中直接返回 JSON 数据时使用
     *
     * @param response HttpServletResponse 对象
     * @param json     JSON 字符串
     * @throws IOException IO 异常
     */
    public static void writeJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }

    /**
     * 输出 JSON 响应（使用当前线程的 Response）
     */
    public static void writeJson(String json) throws IOException {
        HttpServletResponse response = getResponse();
        if (response != null) {
            writeJson(response, json);
        }
    }

    /**
     * 输出错误响应
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public static void writeError(int code, String message) throws IOException {
        String json = String.format("{\"code\":%d,\"message\":\"%s\"}", code, message);
        writeJson(json);
    }

    // ==================== URL 编码/解码 ====================

    /**
     * URL 编码（UTF-8）
     */
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * URL 解码（UTF-8）
     */
    public static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}