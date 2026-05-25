package com.xiyao.trace.config;

import com.xiyao.trace.context.TraceContext;
import com.xiyao.trace.filter.TraceFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 链路追踪自动配置
 * <p>
 * 配置 TraceFilter 和 TraceAspect，
 * 并将 traceId 注入到 MDC 日志上下文。
 *
 * @author xiyao
 * @see TraceProperties
 * @see TraceFilter
 * @see TraceAspect
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TraceProperties.class)
@ConditionalOnProperty(value = "trace.enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfig implements WebMvcConfigurer {

    private final TraceProperties properties;

    public TraceAutoConfig(TraceProperties properties) {
        this.properties = properties;
    }

    /**
     * 注册 traceId 日志拦截器
     * <p>
     * 将 traceId 注入到 MDC，日志输出时自动携带 traceId。
     *
     * @return HandlerInterceptor
     */
    @Bean
    public HandlerInterceptor traceIdLogInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                TraceContext context = TraceContext.get();
                if (context != null && StrUtil.isNotBlank(context.getTraceId())) {
                    // 将 traceId 存入 MDC，日志输出时自动携带
                    MDC.put("traceId", context.getTraceId());
                }
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        Object handler, Exception ex) {
                // 清理 MDC
                MDC.remove("traceId");
            }
        };
    }

    /**
     * 注册日志拦截器到 Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceIdLogInterceptor())
                .addPathPatterns("/**")
                .order(Integer.MAX_VALUE);  // 最后执行，确保 traceId 已设置
    }

    private static class StrUtil {
        public static boolean isNotBlank(String str) {
            return str != null && !str.trim().isEmpty();
        }
    }
}