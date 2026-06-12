package com.xiyao.log.config;

import com.xiyao.log.aspect.LogAspect;
import com.xiyao.log.filter.TraceFilter;
import com.xiyao.log.listener.LogListener;
import com.xiyao.log.properties.LogProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 *     <li>配置项 log.enable=true（默认值为 true，可不配置）</li>
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
 *   enable: true  # 默认 true，可不配置
 * }</pre>
 *
 * @author xiyao
 * @see TraceFilter
 * @see LogAspect
 * @see LogListener
 */
@Configuration
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnProperty(value = "system.log.enable", havingValue = "true", matchIfMissing = true)
public class LogAutoConfig {

    /**
     * 注册链路追踪过滤器
     * <p>
     * 使用 FilterRegistrationBean 注册 Filter，确保：
     * <ul>
     *     <li>过滤器在 Spring MVC 过滤器链中生效</li>
     *     <li>优先级最高（Ordered.HIGHEST_PRECEDENCE），最先执行</li>
     * </ul>
     *
     * @return FilterRegistrationBean 实例
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistrationBean() {
        // 创建 Filter 注册 Bean
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        // 设置过滤器实例
        registration.setFilter(new TraceFilter());
        // 设置优先级最高，确保最先执行，最后结束
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * 日志切面
     * <p>
     * AOP 切面Bean，用于拦截标注 @Log 注解的方法。
     * 核心功能：
     * <ul>
     *     <li>环绕通知拦截方法执行</li>
     *     <li>自动获取用户信息、请求信息</li>
     *     <li>计算方法执行耗时</li>
     *     <li>发布操作日志事件</li>
     * </ul>
     *
     * @return LogAspect 实例
     */
    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    /**
     * 日志监听器
     * <p>
     * 监听 LogOperationEvent 和 LogLoginEvent 事件，
     * 异步保存到数据库（操作日志/审计日志 → log_operation，认证日志 → log_login）。
     *
     * @return LogListener 实例
     */
    @Bean
    public LogListener logListener() {
        return new LogListener();
    }
}