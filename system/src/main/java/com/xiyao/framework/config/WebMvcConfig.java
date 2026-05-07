package com.xiyao.framework.config;

import com.xiyao.framework.converter.MyEnumConverterFactory;
import com.xiyao.framework.resolver.CurrentUserArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Web MVC 配置类
 **/
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping("/**")
                // 置允许跨域请求的域名
                .allowedOriginPatterns("*")
                // 是否允许证书（cookies）
                .allowCredentials(true)
                // 设置允许的请求头
                .allowedHeaders("*")
                // 设置允许的方法
                .allowedMethods("*")
                // 跨域允许时间
                .maxAge(3600);
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        // TODO: 后续扩展 - TraceId 拦截器（记录请求参数、耗时等）
        // registry.addInterceptor(new TraceIdInterceptor()).addPathPatterns("/**").order(1);
        // TODO: 后续扩展 - 请求日志拦截器（记录请求参数、耗时等）
        // registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**").order(2);
        // TODO: 后续扩展 - 限流拦截器（使用Resilience4j注解替代，暂不需要）
        // registry.addInterceptor(new RateLimitInterceptor()).addPathPatterns("/**").order(3);
    }

    /**
     * 参数解析器配置
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 后台管理用户参数解析器（@CurrentUser）
        resolvers.add(new CurrentUserArgumentResolver());
    }

    /**
     * 类型转换器配置
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 枚举转换器（您的原有配置）
        registry.addConverterFactory(new MyEnumConverterFactory());

        // ==================== String → LocalDateTime ====================
        registry.addConverter((Converter<String, LocalDateTime>) source -> {
            if (source.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.warn("日期参数转换失败: {}", source);
                return null;
            }
        });

        // ==================== String → LocalDate ====================
        registry.addConverter((Converter<String, LocalDate>) source -> {
            if (source.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.warn("日期参数转换失败: {}", source);
                return null;
            }
        });

        // ==================== String → LocalTime ====================
        registry.addConverter((Converter<String, LocalTime>) source -> {
            if (source.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalTime.parse(source, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (Exception e) {
                log.warn("时间参数转换失败: {}", source);
                return null;
            }
        });
    }
}
