package com.xiyao.log.config;

import com.xiyao.log.aspect.LogAspect;
import com.xiyao.log.filter.TraceFilter;
import com.xiyao.log.listener.LogListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 日志模块自动配置类
 * <p>
 * 负责在 Spring Boot 启动时自动装配日志（Log）相关的组件。
 * 通过 @ConditionalOnProperty 控制是否启用日志模块。
 *
 * <p>
 * <b>装配条件：</b>
 * <ul>
 *     <li>配置项 log.enabled=true（默认值为 true，可不配置）</li>
 * </ul>
 *
 * <p>
 * <b>自动装配的组件：</b>
 * <ul>
 *     <li>TraceFilter：链路追踪过滤器，生成/传递 traceId</li>
 *     <li>LogAspect：日志切面，拦截 @Log 注解的方法</li>
 *     <li>LogListener：日志监听器，异步保存操作/审计/认证日志</li>
 * </ul>
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * log:
 *   enabled: true
 * }</pre>
 *
 * @author xiyao
 */
@Configuration
@AutoConfigureAfter
@ConditionalOnProperty(value = "log.enabled", havingValue = "true", matchIfMissing = true)
public class LogAutoConfig {

    /**
     * 注册链路追踪过滤器
     * <p>
     * 使用 FilterRegistrationBean 注册，确保过滤器在 Spring MVC 过滤器链中生效。
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistrationBean() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 最高优先级
        return registration;
    }

    /**
     * 日志切面
     */
    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    /**
     * 日志监听器
     */
    @Bean
    public LogListener logListener() {
        return new LogListener();
    }
}