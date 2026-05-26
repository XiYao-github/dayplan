package com.xiyao.governance.config;

import com.xiyao.common.utils.RedisUtils;
import com.xiyao.governance.core.bulkhead.BulkheadAspect;
import com.xiyao.governance.core.circuit.CircuitBreakerAspect;
import com.xiyao.governance.core.fallback.FallbackAspect;
import com.xiyao.governance.core.nosubmit.NoRepeatSubmitAspect;
import com.xiyao.governance.core.ratelimit.RateLimitAspect;
import com.xiyao.governance.core.retry.RetryAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 治理模块自动配置类
 *
 * @author xiyao
 * @see GovernanceProperties
 */
@Configuration
@EnableConfigurationProperties(GovernanceProperties.class)
@ConditionalOnProperty(value = "governance.enabled", havingValue = "true", matchIfMissing = true)
public class GovernanceAutoConfig {

    // ==================== 限流组件 ====================

    @Bean
    public RateLimitAspect rateLimitAspect(GovernanceProperties properties) {
        return new RateLimitAspect(properties);
    }

    // ==================== 熔断组件 ====================

    @Bean
    public CircuitBreakerAspect circuitBreakerAspect(GovernanceProperties properties) {
        return new CircuitBreakerAspect(properties);
    }

    // ==================== 隔离组件 ====================

    @Bean
    public BulkheadAspect bulkheadAspect(GovernanceProperties properties) {
        return new BulkheadAspect(properties);
    }

    // ==================== 重试组件 ====================

    @Bean
    public RetryAspect retryAspect(GovernanceProperties properties) {
        return new RetryAspect(properties);
    }

    // ==================== 降级组件 ====================

    @Bean
    public FallbackAspect fallbackAspect() {
        return new FallbackAspect();
    }

    // ==================== 防重提交组件 ====================

    @Bean
    public NoRepeatSubmitAspect noRepeatSubmitAspect(RedisUtils redisUtils) {
        return new NoRepeatSubmitAspect(redisUtils);
    }
}
