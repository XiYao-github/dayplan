package com.xiyao.governance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 治理模块全局配置
 * <p>
 * 提供限流、熔断、隔离、重试的全局默认配置。
 * 通过 application.yml 的 governance 前缀进行配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * governance:
 *   rate-limit:
 *     permits-per-second: 100
 *     max-burst-requests: 50
 *     message: "请求过于频繁，请稍后再试"
 *   circuit-breaker:
 *     failure-rate-threshold: 5
 *     success-rate-threshold: 3
 *     window-size-millis: 60000
 *     break-duration-millis: 30000
 *     error-rate-threshold: 50
 *     min-request-number: 10
 *   bulkhead:
 *     core-size: 10
 *     max-size: 20
 *     queue-capacity: 100
 *   retry:
 *     max-attempts: 3
 *     interval-millis: 1000
 *     multiplier: 2.0
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "governance")
public class GovernanceProperties {

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 熔断配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    /**
     * 隔离配置
     */
    private BulkheadConfig bulkhead = new BulkheadConfig();

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 限流全局配置
     * <p>
     * 控制接口的调用频率，防止系统被突发流量冲垮。
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 每秒令牌数
         * <p>
         * 令牌桶的令牌补充速率，即每秒允许的最大调用次数。
         */
        private double permitsPerSecond = 100;
        /**
         * 突发容量
         * <p>
         * 令牌桶的最大容量，允许一次性处理的最大请求数。
         */
        private int maxBurstRequests = 50;
        /**
         * 限流提示消息
         */
        private String message = "请求过于频繁，请稍后再试";
    }

    /**
     * 熔断全局配置
     * <p>
     * 当下游服务故障时快速失败，防止故障扩散。
     */
    @Data
    public static class CircuitBreakerConfig {
        /**
         * 失败阈值次数
         * <p>
         * 在时间窗口内失败次数达到此值触发熔断。
         */
        private int failureRateThreshold = 5;
        /**
         * 成功阈值次数
         * <p>
         * 半开状态下成功次数达到此值则关闭熔断。
         */
        private int successRateThreshold = 3;
        /**
         * 时间窗口大小（毫秒）
         * <p>
         * 统计失败/成功次数的时间范围。
         */
        private long windowSizeMillis = 60000;
        /**
         * 熔断持续时间（毫秒）
         * <p>
         * 熔断开启后持续的时间，之后进入半开状态尝试恢复。
         */
        private long breakDurationMillis = 30000;
        /**
         * 错误率阈值（百分比）
         * <p>
         * 当错误率达到此值时触发熔断。
         */
        private double errorRateThreshold = 50;
        /**
         * 最小请求数
         * <p>
         * 请求数达到此值才开始计算错误率。
         */
        private int minRequestNumber = 10;
    }

    /**
     * 隔离全局配置
     * <p>
     * 使用信号量或线程池隔离并发调用，防止资源耗尽。
     */
    @Data
    public static class BulkheadConfig {
        /**
         * 核心线程数
         * <p>
         * 线程池保持的最小线程数。
         */
        private int coreSize = 10;
        /**
         * 最大线程数
         * <p>
         * 线程池允许的最大线程数。
         */
        private int maxSize = 20;
        /**
         * 队列容量
         * <p>
         * 等待队列的最大容量，超过则拒绝执行。
         */
        private int queueCapacity = 100;
    }

    /**
     * 重试全局配置
     * <p>
     * 当调用失败时自动重试，提高系统容错能力。
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         * <p>
         * 包含首次执行在内的最大执行次数。
         */
        private int maxAttempts = 3;
        /**
         * 重试间隔（毫秒）
         * <p>
         * 首次重试前的等待时间。
         */
        private long intervalMillis = 1000;
        /**
         * 指数退避倍数
         * <p>
         * 重试间隔的指数增长倍数。
         * 例如：multiplier=2.0，则间隔为 1000, 2000, 4000...
         */
        private double multiplier = 2.0;
    }
}