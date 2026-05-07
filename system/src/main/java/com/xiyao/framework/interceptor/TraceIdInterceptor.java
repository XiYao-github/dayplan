package com.xiyao.framework.interceptor;

import com.xiyao.framework.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId 拦截器
 * <p>
 * 为每个请求生成唯一的 TraceId，并存入 MDC，以便在日志中追踪请求链路。
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
@Slf4j
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 1. 从请求头获取已有的 TraceId（支持网关传递）
        String traceId = request.getHeader(TraceIdUtils.HEADER_TRACE_ID);

        // 2. 生成并设置 TraceId
        traceId = TraceIdUtils.generateAndSet(traceId);

        // 3. 将 TraceId 添加到响应头，方便前端关联
        response.setHeader(TraceIdUtils.HEADER_TRACE_ID, traceId);

        // 4. 记录请求开始日志（可选，方便排查问题）
        log.debug("请求进入: method={}, uri={}, traceId={}",
                request.getMethod(), request.getRequestURI(), traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 请求结束后清除 TraceId，避免线程复用时的污染
        if (ex != null) {
            log.debug("请求结束: uri={}, 异常={}", request.getRequestURI(), ex.getMessage());
        }
        TraceIdUtils.clear();
    }
}