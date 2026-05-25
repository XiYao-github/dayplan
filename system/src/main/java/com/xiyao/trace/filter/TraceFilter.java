package com.xiyao.trace.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.trace.config.TraceProperties;
import com.xiyao.trace.context.TraceContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 链路追踪过滤器
 * <p>
 * 职责：
 * <ul>
 *     <li>从请求头中提取 traceId，如果不存在则生成新的</li>
 *     <li>创建 TraceContext 并存入 ThreadLocal</li>
 *     <li>将 traceId 返回给前端（通过响应头）</li>
 *     <li>请求结束时清理 ThreadLocal</li>
 * </ul>
 *
 * <p>
 * <b>执行时机：</b>
 * 此过滤器在 Security 过滤器之前执行，确保 traceId 在请求一开始就被创建。
 *
 * @author xiyao
 * @see TraceContext
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TraceFilter implements Filter {

    /** 追踪上下文存储键 */
    private static final String TRACE_CONTEXT_KEY = "TRACE_CONTEXT";

    private final TraceProperties properties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取或创建 traceId
        String traceId = getOrCreateTraceId(httpRequest);

        // 创建追踪上下文
        TraceContext context = new TraceContext();
        context.setTraceId(traceId);
        context.setSpanId(TraceContext.generateSpanId());

        // 存入 ThreadLocal
        TraceContext.set(context);

        try {
            // 设置响应头，返回 traceId 给前端
            String responseHeader = properties.getTraceId().getResponseHeader();
            if (StrUtil.isNotBlank(responseHeader)) {
                httpResponse.setHeader(responseHeader, traceId);
            }

            // 打印入口日志
            log.debug("请求进入 - traceId={}, uri={}, method={}",
                    traceId, httpRequest.getRequestURI(), httpRequest.getMethod());

            // 继续执行过滤器链
            chain.doFilter(request, response);

        } finally {
            // 请求结束，清理 ThreadLocal
            TraceContext.clear();
            log.debug("请求结束 - traceId={}", traceId);
        }
    }

    /**
     * 获取或创建 traceId
     * <p>
     * 优先从请求头中获取（支持上游传入），
     * 如果请求头中没有，则生成新的 traceId。
     *
     * @param request HTTP 请求
     * @return traceId
     */
    private String getOrCreateTraceId(HttpServletRequest request) {
        String headerName = properties.getTraceId().getHeader();
        String traceId = request.getHeader(headerName);

        // 如果请求头中没有 traceId，则生成新的
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
            log.debug("生成新的 traceId: {}", traceId);
        } else {
            log.debug("使用上游传来的 traceId: {}", traceId);
        }

        return traceId;
    }
}