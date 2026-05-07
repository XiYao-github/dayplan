package com.xiyao.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyao.framework.interceptor.TraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
 * <p>
 * WebMvcConfigurer 是 Spring MVC 的配置接口，所有配置方法都有默认实现，
 * 我们只需要重写需要定制的方法即可。
 * </p>
 *
 * <p>
 * 配置内容详解：
 * <ul>
 *   <li>addCorsMappings：配置跨域请求规则</li>
 *   <li>addInterceptors：注册拦截器（请求处理前/后执行）</li>
 *   <li>addFormatters：注册参数类型转换器（如 String → LocalDateTime）</li>
 *   <li>configureMessageConverters：配置消息转换器（如 JSON 序列化）</li>
 * </ul>
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor  // 生成构造器注入 final 字段
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注入我们自定义的 ObjectMapper（来自 JacksonConfig）
     * 用于消息转换器，确保 JSON 序列化规则一致
     */
    private final ObjectMapper objectMapper;

    /**
     * 配置跨域请求（CORS：Cross-Origin Resource Sharing）
     * <p>
     * 什么是跨域？
     * 浏览器安全策略：当网页的协议、域名、端口任意一个不同时，不允许 AJAX 请求。
     * 例如：前端 localhost:8080 请求后端 localhost:8081，属于跨域。
     * </p>
     *
     * <p>
     * 公安网特殊说明：
     * 公安网内一般是内网访问，跨域需求较少。如果需要跨域，应该配置具体的内网 IP，
     * 而不是允许所有域名（allowedOriginPatterns = "*" 存在安全风险）。
     * </p>
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")           // 对所有接口生效
                // 允许跨域的来源（生产环境应配置具体 IP，如 "http://192.168.1.100"）
                .allowedOriginPatterns("*")  // 开发环境可暂时允许所有
                // 是否允许携带 Cookie（前端需要设置 withCredentials=true）
                .allowCredentials(true)
                // 允许的请求头（* 表示允许所有）
                .allowedHeaders("*")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 预检请求（OPTIONS）的缓存时间（秒）
                // 浏览器会先发 OPTIONS 询问是否允许跨域，这个配置可以减少预检请求次数
                .maxAge(3600);

        log.info("CORS 跨域配置完成，允许所有来源（生产环境需限制）");
    }

    /**
     * 注册拦截器
     * <p>
     * 拦截器的作用：在请求进入 Controller 之前或之后执行逻辑。
     * 执行顺序：按 order 从小到大执行。
     * </p>
     *
     * <p>
     * 常用拦截器场景：
     * <ul>
     *   <li>TraceIdInterceptor：为每个请求生成唯一追踪 ID</li>
     *   <li>LogInterceptor：记录请求日志（URL、参数、耗时）</li>
     *   <li>RateLimitInterceptor：限流检查</li>
     *   <li>RepeatSubmitInterceptor：防重复提交</li>
     * </ul>
     * </p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加 TraceId 拦截器
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns("/**")      // 对所有路径生效
                .order(1);                   // 优先级最高（数字越小越先执行）

        log.info("拦截器注册完成：TraceIdInterceptor");

        // 后续可以添加更多拦截器：
        // registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**").order(2);
        // registry.addInterceptor(new RateLimitInterceptor()).addPathPatterns("/**").order(3);
    }

    /**
     * 注册参数类型转换器
     * <p>
     * 问题：Controller 方法参数如果是 LocalDateTime 类型，前端传字符串 "2024-01-15 14:30:00"
     * 默认情况下 Spring 无法自动转换，会报错。
     * </p>
     *
     * <p>
     * 解决：注册一个 Converter，将 String 转换为 LocalDateTime。
     * 这样 Controller 就可以直接写：
     * public Result getList(@RequestParam LocalDateTime startTime)
     * </p>
     *
     * <p>
     * 除了 LocalDateTime，还可以配置枚举转换、LocalDate、LocalTime 等。
     * </p>
     *
     * @param registry 格式化器注册器
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
                    return LocalDateTime.parse(source,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
                    return LocalDate.parse(source,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
                    return LocalTime.parse(source,
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                } catch (Exception e) {
                    log.warn("时间参数转换失败: {}", source);
                    return null;
                }
            }
        });

        log.info("参数转换器注册完成：String → LocalDateTime/LocalDate/LocalTime");
    }

    /**
     * 配置消息转换器（HTTP 请求/响应体的序列化）
     * <p>
     * 消息转换器的作用：将 Java 对象转换为 HTTP 响应体（JSON），
     * 以及将 HTTP 请求体（JSON）转换为 Java 对象。
     * </p>
     *
     * <p>
     * 为什么需要配置？
     * 我们需要使用自定义的 ObjectMapper（来自 JacksonConfig），
     * 确保 Long 转 String、日期格式化等配置在 HTTP 请求/响应时生效。
     * </p>
     *
     * @param converters 消息转换器列表（Spring 默认已包含多个）
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建 Jackson 消息转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 使用我们自定义的 ObjectMapper
        converter.setObjectMapper(objectMapper);
        // 添加到列表首位（优先级最高）
        converters.add(0, converter);

        log.info("消息转换器配置完成：使用自定义 Jackson 序列化规则");
    }
}
