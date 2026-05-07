package com.xiyao.framework.utils;

import cn.hutool.core.util.IdUtil;
import org.slf4j.MDC;

/**
 * TraceId 工具类
 * <p>
 * 用于在分布式日志中追踪一次请求的全链路。
 * TraceId 在请求入口生成，贯穿整个请求处理过程，最终输出到日志中。
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
public class TraceIdUtils {

    /** MDC 中存储 TraceId 的 Key（日志配置中使用 %X{traceId} 输出） */
    public static final String TRACE_ID_KEY = "traceId";

    /** 请求头中传递 TraceId 的 Key（用于网关传递） */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 生成并设置 TraceId
     * <p>
     * 如果请求头中已有 TraceId（如从网关传入），则直接使用，否则重新生成一个新的。
     * </p>
     *
     * @param existTraceId 已有的 TraceId（从请求头获取），可为空
     * @return 当前设置的 TraceId
     */
    public static String generateAndSet(String existTraceId) {
        String traceId = existTraceId;
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = IdUtil.fastSimpleUUID();
        }
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /**
     * 生成并设置 TraceId（不传入已有值）
     */
    public static String generateAndSet() {
        return generateAndSet(null);
    }

    /**
     * 获取当前 TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除 TraceId（请求结束时调用，防止线程复用污染）
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}