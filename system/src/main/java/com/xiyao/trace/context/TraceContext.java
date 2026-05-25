package com.xiyao.trace.context;

import lombok.Data;

/**
 * 链路追踪上下文
 * <p>
 * 存储当前线程的追踪信息，包括 traceId、spanId 等。
 * 基于 ThreadLocal 实现，线程隔离。
 *
 * @author xiyao
 */
@Data
public class TraceContext {

    /** 追踪链 ID（整个请求链路的唯一标识） */
    private String traceId;

    /** 当前 span ID（当前操作的唯一标识） */
    private String spanId;

    /** 父 span ID（调用链上游的 span） */
    private String parentSpanId;

    /** 追踪开关 */
    private boolean enable = true;

    /** 追踪上下文存储线程变量 */
    private static final ThreadLocal<TraceContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置追踪上下文
     */
    public static void set(TraceContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取追踪上下文
     */
    public static TraceContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前上下文，如果不存在则创建新的
     */
    public static TraceContext getOrCreate() {
        TraceContext context = CONTEXT.get();
        if (context == null) {
            context = new TraceContext();
            CONTEXT.set(context);
        }
        return context;
    }

    /**
     * 清除追踪上下文
     * <p>
     * 必须在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 生成新的 spanId
     *
     * @return 基于时间戳和随机数的 spanId
     */
    public static String generateSpanId() {
        return System.nanoTime() + "-" + (int) (Math.random() * 9999);
    }
}