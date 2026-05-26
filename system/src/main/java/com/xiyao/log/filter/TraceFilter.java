package com.xiyao.log.filter;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * 链路追踪过滤器
 * <p>
 * 功能：
 * <ul>
 *     <li>为每个请求生成唯一的 traceId（UUID 格式）</li>
 *     <li>支持上游传入 traceId（通过 X-Trace-Id 请求头）</li>
 *     <li>将 traceId 放入 MDC，日志自动携带</li>
 *     <li>响应头返回 traceId，方便前端排查问题</li>
 * </ul>
 *
 * <p>
 * <b>使用方式：</b>
 * <ul>
 *     <li>请求头传入：X-Trace-Id: abc123</li>
 *     <li>响应头返回：X-Trace-Id: abc123</li>
 *     <li>日志中自动携带：通过 MDC.get("traceId") 获取</li>
 * </ul>
 *
 * @author xiyao
 */
public class TraceFilter implements Filter {

    /**
     * TraceId 请求头/响应头名称
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC 中 traceId 的 key
     */
    public static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取或生成 traceId
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = IdUtil.fastSimpleUUID();
        }

        try {
            // 放入 MDC，日志自动携带
            MDC.put(TRACE_ID_KEY, traceId);

            // 响应头返回 traceId
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            // 继续过滤链
            chain.doFilter(request, response);
        } finally {
            // 清理 MDC
            MDC.remove(TRACE_ID_KEY);
        }
    }
}