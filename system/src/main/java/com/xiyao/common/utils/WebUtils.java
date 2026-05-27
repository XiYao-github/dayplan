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
import java.util.HashMap;
import java.util.Map;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebUtils {

    public static final String SEPARATOR = ",";

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

    // ==================== 请求参数 ====================

    /**
     * 获取指定名称的 String 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取指定名称的 String 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static String getParameter(String name, String defaultValue) {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的 Long 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Long getParameterToLong(String name) {
        return Convert.toLong(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的 Long 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Long getParameterToLong(String name, Long defaultValue) {
        return Convert.toLong(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Boolean getParameterToBool(String name) {
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
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
            result.put(entry.getKey(), String.join(SEPARATOR, values));
        }
        return result;
    }

    // ==================== 响应输出 ====================

    /**
     * 状态码和业务消息返回
     *
     * @param response 渲染对象
     * @param code     状态码
     * @param msg      业务消息
     */
    public static void print(HttpServletResponse response, Integer code, String msg) {
        print(response, JSONUtil.toJsonStr(Result.result(code, msg)));
    }


    /**
     * JSON 格式返回
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
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