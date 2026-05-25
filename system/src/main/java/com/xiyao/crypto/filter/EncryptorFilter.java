package com.xiyao.crypto.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.crypto.filter.wrapper.DecryptRequestWrapper;
import com.xiyao.crypto.filter.wrapper.EncryptResponseWrapper;
import com.xiyao.crypto.properties.EncryptorApi;
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
 * <p>
 * 职责：
 * <ul>
 *     <li>拦截配置的 API 请求，进行请求解密和响应加密</li>
 *     <li>使用 SM2/SM4 国密算法保护数据传输安全</li>
 *     <li>支持路径排除，不加密特定接口</li>
 * </ul>
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>检查请求路径是否在排除列表中，是则放行</li>
 *     <li>检查是否为 POST 请求，非 POST 放行</li>
 *     <li>检查请求头是否携带加密标识（有加密标识才处理）</li>
 *     <li>将请求体解密后包装到 DecryptRequestWrapper</li>
 *     <li>将响应加密后包装到 EncryptResponseWrapper</li>
 *     <li>放行请求，后续 Controller 处理解密后的数据</li>
 * </ol>
 *
 * <p>
 * <b>配置项：</b>
 * <pre>{@code
 * encryptor-api:
 *   enabled: true
 *   public-key: "公钥"
 *   private-key: "私钥"
 *   header-flag: "X-Encrypt"
 *   include-paths:
 *     - /api/**
 *   exclude-paths:
 *     - /api/public/**
 * }</pre>
 *
 * @author xiyao
 */
@AllArgsConstructor
public class EncryptorFilter implements Filter {

    /**
     * 加解密配置属性
     */
    private final EncryptorApi properties;

    /**
     * 过滤器核心方法
     *
     * @param request   HTTP 请求
     * @param response HTTP 响应
     * @param chain    过滤器链
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        // ========== 路径处理 ==========
        // 获取请求路径（去除 contextPath）
        String requestUri = servletRequest.getRequestURI();
        String contextPath = servletRequest.getContextPath();
        String path = requestUri.substring(contextPath.length());

        // ========== 排除路径检查 ==========
        List<String> excludePaths = properties.getExcludePaths();
        if (CollUtil.isNotEmpty(excludePaths)) {
            AntPathMatcher matcher = new AntPathMatcher();
            for (String pattern : excludePaths) {
                // 匹配成功，放行（不加密）
                if (matcher.match(pattern, path)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // ========== 请求方法检查 ==========
        // 仅处理 POST 请求
        if (!HttpMethod.POST.matches(servletRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // ========== 加密标识检查 ==========
        // 必须携带特定 Header 才进行加解密
        String headerValue = servletRequest.getHeader(properties.getHeaderFlag());
        if (StrUtil.isBlank(headerValue)) {
            chain.doFilter(request, response);
            return;
        }

        // ========== 加解密处理 ==========
        try {
            // 创建请求解密包装器（内部自动解密请求体）
            DecryptRequestWrapper requestWrapper = new DecryptRequestWrapper(
                    servletRequest, properties.getPrivateKey(), properties.getHeaderFlag());

            // 创建响应加密包装器（待处理完成后加密响应体）
            EncryptResponseWrapper responseWrapper = new EncryptResponseWrapper(servletResponse);

            // 放行请求（使用包装后的请求和响应）
            chain.doFilter(requestWrapper, responseWrapper);

            // 重置响应（清除之前写入的内容，准备加密）
            servletResponse.reset();

            // 对响应内容进行加密并输出
            responseWrapper.encryptContent(servletResponse, properties.getPublicKey(), properties.getHeaderFlag());
        } catch (Exception e) {
            throw new RuntimeException("过滤器传输加解密失败", e);
        }
    }
}