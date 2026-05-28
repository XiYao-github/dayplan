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
 * 为每个 HTTP 请求生成或接收唯一的 traceId，用于全链路追踪和问题排查。
 *
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>为每个请求生成唯一的 traceId（UUID 格式）</li>
 *     <li>支持上游传入 traceId（通过 X-Trace-Id 请求头）</li>
 *     <li>将 traceId 放入 MDC，日志自动携带</li>
 *     <li>响应头返回 traceId，方便前端排查问题</li>
 * </ul>
 *
 * <p>
 * <b>使用方式：</b>
 * <pre>
 * 请求头传入：X-Trace-Id: abc123（可选，不传则自动生成）
 * 响应头返回：X-Trace-Id: abc123
 * 日志中自动携带：通过 MDC.get("traceId") 获取
 * </pre>
 *
 * <p>
 * <b>应用场景：</b>
 * <ul>
 *     <li>分布式系统问题定位：通过 traceId 串联整个请求链路</li>
 *     <li>日志聚合分析：同一 traceId 的日志可以快速关联</li>
 *     <li>性能分析：traceId 关联请求入口到响应的全流程</li>
 * </ul>
 *
 * @author xiyao
 * @see MDC
 */
public class TraceFilter implements Filter {

    /**
     * TraceId 请求头/响应头名称
     * <p>
     * 统一使用 X-Trace-Id 作为请求头和响应头的名称。
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC 中 traceId 的 key
     * <p>
     * 在 MDC 中存储 traceId 的键名，供日志框架自动获取。
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 过滤处理
     * <p>
     * 核心过滤逻辑：
     * <ol>
     *     <li>检查请求头是否有 X-Trace-Id</li>
     *     <li>有则使用，无则生成新的 UUID</li>
     *     <li>放入 MDC，供日志框架自动携带</li>
     *     <li>响应头返回 traceId</li>
     *     <li>finally 中清理 MDC</li>
     * </ol>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param chain    过滤器链
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 强制转换为 HttpServletRequest/Response
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 从请求头获取 traceId，为空则生成新的 UUID
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            // 生成唯一的跟踪 ID
            traceId = IdUtil.fastSimpleUUID();
        }

        try {
            // 将 traceId 放入 MDC
            // 日志框架（如 Logback）配置 %X{traceId} 可以自动获取并打印
            MDC.put(TRACE_ID_KEY, traceId);

            // 响应头返回 traceId，方便前端排查问题时提供
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            // 继续执行后续过滤器
            chain.doFilter(request, response);

        } finally {
            // 清理 MDC，避免线程池复用时携带上一个请求的 traceId
            MDC.remove(TRACE_ID_KEY);
        }
    }
}