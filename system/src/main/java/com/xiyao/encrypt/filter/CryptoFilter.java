package com.xiyao.encrypt.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.properties.EncryptorApi;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;


/**
 * 加解密过滤器
 */
@AllArgsConstructor
public class CryptoFilter implements Filter {

    private final EncryptorApi properties;

    /**
     * 过滤器方法
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        // 获取当前请求路径
        String requestUri = servletRequest.getRequestURI();
        String contextPath = servletRequest.getContextPath();
        String path = requestUri.substring(contextPath.length());
        // 排除路径检查
        List<String> excludePaths = properties.getExcludePaths();
        if (CollUtil.isNotEmpty(excludePaths)) {
            // Spring 路径匹配器
            AntPathMatcher matcher = new AntPathMatcher();
            for (String pattern : excludePaths) {
                // 匹配成功，放行
                if (matcher.match(pattern, path)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }
        // 只拦截 POST 请求
        if (!HttpMethod.POST.matches(servletRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        // 检查加密请求头
        String headerValue = servletRequest.getHeader(properties.getHeaderFlag());
        if (StrUtil.isBlank(headerValue)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            // 请求解密包装
            DecryptRequestWrapper requestWrapper = new DecryptRequestWrapper(servletRequest, properties.getPrivateKey(), properties.getHeaderFlag());
            // 响应加密包装
            EncryptResponseWrapper responseWrapper = new EncryptResponseWrapper(servletResponse);
            // 过滤器放行
            chain.doFilter(requestWrapper, responseWrapper);
            // 重置响应
            responseWrapper.reset();
            // 响应加密
            responseWrapper.encryptContent(servletResponse, properties.getPublicKey(), properties.getHeaderFlag());
        } catch (Exception e) {
            throw new RuntimeException("过滤器传输加解密失败");
        }
    }

}
