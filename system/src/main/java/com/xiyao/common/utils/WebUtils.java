package com.xiyao.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.xiyao.common.utils.data.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Web 工具类（前后端分离版）
 * <p>
 * 针对前后端分离架构，主要提供：
 * - 请求/响应对象获取
 * - 请求参数获取
 * - 请求头获取
 * - 客户端 IP 获取
 * - JSON 响应输出
 * <p>
 * 不包含 Session、Cookie、重定向等传统 Web 相关方法
 *
 * @author xiyao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebUtils {

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

    // ==================== 请求基本信息 ====================

    /**
     * 获取请求方式（GET、POST、PUT、DELETE 等）
     * <p>
     * 返回 HTTP 请求方法，如 GET、POST、PUT、DELETE 等。
     *
     * @return 请求方法，未获取到返回 null
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
     * 获取请求 URL
     * <p>
     * 例如：http://localhost:8080
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURL().toString() : null;
    }

    /**
     * 获取查询参数字符串（URL 中 ? 后面的部分）
     */
    public static String getQueryString() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getQueryString() : null;
    }

    /**
     * 获取完整请求 URL（包含查询参数）
     * <p>
     * 例如：http://localhost:8080/api/user?name=zhangsan
     */
    public static String getFullRequestUrl() {
        String requestUrl = getRequestUrl();
        String queryString = getQueryString();
        return requestUrl != null && queryString != null ? requestUrl + "?" + queryString : null;
    }

    // ==================== 请求头 ====================

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

    /**
     * 获取 Authorization 头（用于 JWT Token）
     * <p>
     * 通常格式：Bearer xxxxx
     */
    public static String getAuthorization() {
        String bearerToken = getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.replace("Bearer ", "");
        }
        return bearerToken;
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
     * 获取 Referer 请求头（来源页面）
     */
    public static String getReferer() {
        return getHeader("Referer");
    }

    /**
     * 获取 Origin 请求头（跨域请求来源）
     */
    public static String getOrigin() {
        return getHeader("Origin");
    }

    // ==================== 客户端信息 ====================

    /**
     * 获取客户端 IP 地址
     * <p>
     * 考虑 Nginx 反向代理，依次从以下 Header 中获取：
     * X-Forwarded-For -> X-Real-IP -> request.getRemoteAddr()
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
     * 获取客户端端口号
     */
    public static Integer getRemotePort() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRemotePort() : null;
    }

    /**
     * 获取服务器名称（域名）
     */
    public static String getServerName() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getServerName() : null;
    }

    /**
     * 获取服务器端口
     */
    public static Integer getServerPort() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getServerPort() : null;
    }

    /**
     * 获取服务器信息（IP:端口）
     */
    public static String getServerInfo() {
        String serverName = getServerName();
        Integer serverPort = getServerPort();
        if (serverName == null || serverPort == null) {
            return null;
        }
        return serverName + ":" + serverPort;
    }

    /**
     * 获取 User-Agent（客户端类型识别）
     */
    public static String getUserAgent() {
        return getHeader("User-Agent");
    }

    // ==================== 请求参数 ====================

    /**
     * 获取指定名称的 String 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? getRequest().getParameter(name) : null;
    }

    /**
     * 获取指定名称的 String 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static String getParameter(String name, String defaultValue) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toStr(getRequest().getParameter(name), defaultValue) : defaultValue;
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Integer getParameterToInt(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toInt(getRequest().getParameter(name)) : null;
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toInt(getRequest().getParameter(name), defaultValue) : defaultValue;
    }

    /**
     * 获取指定名称的 Long 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Long getParameterToLong(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toLong(getRequest().getParameter(name)) : null;
    }

    /**
     * 获取指定名称的 Long 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Long getParameterToLong(String name, Long defaultValue) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toLong(getRequest().getParameter(name), defaultValue) : defaultValue;
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Boolean getParameterToBool(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toBool(getRequest().getParameter(name)) : null;
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        HttpServletRequest request = getRequest();
        return request != null ? Convert.toBool(getRequest().getParameter(name), defaultValue) : defaultValue;
    }

    /**
     * 获取所有请求参数（以 Map 的形式返回，值为字符串形式的拼接）
     *
     * @return 请求参数的 Map，键为参数名，值为拼接后的字符串
     */
    public static Map<String, String> getParamMap() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] values = entry.getValue();
            result.put(entry.getKey(), String.join(",", values));
        }
        return result;
    }

    // ==================== 响应输出 ====================

    /**
     * 通过响应输出状态码和业务消息
     * <p>
     * 将 code 和 msg 封装为 JSON 响应体并写入 HttpServletResponse。
     * 响应内容为：{@code {"code": xxx, "msg": "xxx", "data": null}}
     *
     * @param response HttpServletResponse 对象，用于写入响应
     * @param code     HTTP 状态码（如 200、500）
     * @param msg      业务消息（如"请求成功"、"参数错误"）
     */
    public static void print(HttpServletResponse response, Integer code, String msg) {
        print(response, JSONUtil.toJsonStr(Result.result(code, msg)));
    }

    /**
     * 通过响应输出 JSON 字符串
     * <p>
     * 设置响应编码为 UTF-8，Content-Type 为 application/json，
     * 然后将字符串内容写入响应体。通常用于输出 JSON 序列化后的结果。
     *
     * @param response HttpServletResponse 对象
     * @param string   待输出的字符串内容（通常为 JSON 格式）
     */
    public static void print(HttpServletResponse response, String string) {
        try {
            response.setStatus(cn.hutool.http.HttpStatus.HTTP_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== URL 编码/解码 ====================

    /**
     * URL 编码（UTF-8）
     */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * URL 解码（UTF-8）
     */
    public static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}