package com.xiyao.governance.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 治理模块自动配置类
 * <p>
 * 负责在 Spring Boot 启动时自动装配治理（Governance）相关的组件。
 * 通过 @ConfigurationProperties 绑定配置文件中的 governance.* 前缀配置，
 * 并启用 @Aspect 注解的切面类实现限流、熔断、隔离、重试、防重复提交功能。
 *
 * <p>
 * <b>装配条件：</b>
 * <ul>
 *     <li>配置项 governance.enabled=true（默认值为 true，可不配置）</li>
 *     <li>在 GovernanceProperties 类之后装配</li>
 * </ul>
 *
 * <p>
 * <b>自动装配的组件：</b>
 * <ul>
 *     <li>GovernanceProperties：治理配置属性类，绑定 governance.* 配置</li>
 *     <li>RateLimitAspect：限流切面，拦截 @RateLimit 注解的方法</li>
 *     <li>CircuitBreakerAspect：熔断切面，拦截 @CircuitBreaker 注解的方法</li>
 *     <li>BulkheadAspect：隔离切面，拦截 @Bulkhead 注解的方法</li>
 *     <li>RetryAspect：重试切面，拦截 @Retryable 注解的方法</li>
 *     <li>FallbackAspect：降级切面，拦截 @Fallback 注解的方法</li>
 *     <li>NoRepeatSubmitAspect：防重复提交切面，拦截 @NoRepeatSubmit 注解的方法</li>
 * </ul>
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * governance:
 *   enabled: true
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
 * @see GovernanceProperties
 */
@Configuration
@AutoConfigureAfter
@EnableConfigurationProperties(GovernanceProperties.class)
@ConditionalOnProperty(value = "governance.enabled", havingValue = "true", matchIfMissing = true)
public class GovernanceAutoConfig {

    /**
     * 切面类自动注册说明
     * <p>
     * 配置类加载后，各切面 @Aspect 注解的 Bean 将自动注册到 AOP 切面链中。
     * Spring Boot 会自动扫描所有 @Aspect + @Component 的类，无需额外注册 Bean。
     * <p>
     * 切面执行顺序（按注解优先级）：
     * 1. RateLimitAspect（限流，最外层保护）
     * 2. CircuitBreakerAspect（熔断，调用保护）
     * 3. BulkheadAspect（隔离，并发限制）
     * 4. RetryAspect（重试，容错处理）
     * 5. FallbackAspect（降级，最终兜底）
     * 6. NoRepeatSubmitAspect（防重复提交，最后一道防线）
     */
    public GovernanceAutoConfig() {
        // 无需额外初始化逻辑，Spring Boot 自动完成组件扫描和注册
    }
}