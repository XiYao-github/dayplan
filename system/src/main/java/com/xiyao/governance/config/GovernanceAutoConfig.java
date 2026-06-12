package com.xiyao.governance.config;

import com.xiyao.governance.aspectj.RateLimiterAspect;
import com.xiyao.governance.aspectj.RepeatSubmitAspect;
import com.xiyao.governance.properties.GovernanceProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;

@Configuration
@AutoConfigureAfter(RedisConfiguration.class)
@EnableConfigurationProperties(GovernanceProperties.class)
@ConditionalOnProperty(value = "system.governance.enable", havingValue = "true", matchIfMissing = true)
public class GovernanceAutoConfig {

    @Bean
    public RepeatSubmitAspect repeatSubmitAspect() {
        return new RepeatSubmitAspect();
    }

    @Bean
    public RateLimiterAspect rateLimiterAspect() {
        return new RateLimiterAspect();
    }
}
