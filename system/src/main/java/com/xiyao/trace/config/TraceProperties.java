package com.xiyao.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 链路追踪配置属性
 * <p>
 * 通过 application.yml 的 trace.* 前缀配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * trace:
 *   enabled: true
 *   trace-id:
 *     header: "X-Trace-Id"
 *     response-header: "X-Trace-Id"
 *   log:
 *     enable: true
 *     pattern: "[traceId=%s]"
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "trace")
public class TraceProperties {

    /** 是否启用链路追踪 */
    private boolean enabled = true;

    /** traceId 配置 */
    private TraceIdConfig traceId = new TraceIdConfig();

    /** 日志配置 */
    private LogConfig log = new LogConfig();

    @Data
    public static class TraceIdConfig {
        /** 请求头名称（用于接收上游传来的 traceId） */
        private String header = "X-Trace-Id";
        /** 响应头名称（用于返回 traceId 给前端） */
        private String responseHeader = "X-Trace-Id";
    }

    @Data
    public static class LogConfig {
        /** 是否启用日志增强（自动添加 traceId 到日志） */
        private boolean enable = true;
        /** traceId 日志格式 */
        private String pattern = "[traceId=%s]";
    }
}