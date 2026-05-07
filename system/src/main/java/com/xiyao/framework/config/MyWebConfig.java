package com.xiyao.framework.config;

import com.xiyao.framework.interceptor.TraceIdInterceptor;
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
 * 跨域配置（CORS）
 * 拦截器注册（TraceId、限流等）
 * 参数解析器注册（@CurrentUser、@CurrentMpUser等）
 * 类型转换器注册（String → LocalDateTime、枚举等）
 * 静态资源配置（Swagger、文件访问等）
 * 消息转换器配置（JSON序列化）
 **/
@Slf4j
@Configuration
public class MyWebConfig implements WebMvcConfigurer {


    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping("/**")
                // 置允许跨域请求的域名
                .allowedOriginPatterns("*")
                //是否允许证书（cookies）
                .allowCredentials(true)
                //设置允许的请求头
                .allowedHeaders("*")
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TraceId 拦截器（最高优先级，为每个请求生成追踪ID）
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns("/**")
                .order(1);

        // 限流拦截器（按 IP 或用户限流，可根据配置开关）
        // registry.addInterceptor(rateLimitInterceptor)
        //         .addPathPatterns("/**")
        //         .excludePathPatterns(getExcludePaths())
        //         .order(2);
    }

    /**
     * 参数解析器配置
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }

    /**
     * 类型转换器配置
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // ==================== String → LocalDateTime ====================
        registry.addConverter(new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    log.warn("日期参数转换失败: {}", source);
                    return null;
                }
            }
        });

        // ==================== String → LocalDate ====================
        registry.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    log.warn("日期参数转换失败: {}", source);
                    return null;
                }
            }
        });

        // ==================== String → LocalTime ====================
        registry.addConverter(new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalTime.parse(source, DateTimeFormatter.ofPattern("HH:mm:ss"));
                } catch (Exception e) {
                    log.warn("时间参数转换失败: {}", source);
                    return null;
                }
            }
        });
    }
}
