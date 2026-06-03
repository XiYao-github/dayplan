package com.xiyao.framework.config;

import com.xiyao.dict.converter.EnumConverterFactory;
import com.xiyao.framework.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类
 * <p>
 * 配置 Spring MVC 的各项组件，包括：
 * <ul>
 *     <li>跨域资源共享（CORS）配置</li>
 *     <li>拦截器注册</li>
 *     <li>参数解析器</li>
 * </ul>
 *
 * @author xiyao
 * @see WebMvcConfigurer
 */
@Configuration
@EnableWebMvc  // 完全接管Spring MVC配置（若不需要可注释，使用默认配置）
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置跨域资源共享（CORS）
     * <p>
     * 允许前端页面从不同域名发起 AJAX 请求。
     * 当前配置允许所有来源（生产环境建议指定具体域名）。
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许跨域的来源，* 表示所有（生产环境建议指定具体域名如 https://example.com）
                .allowedOriginPatterns("*")
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                // 允许的请求头（* 表示所有）
                .allowedHeaders("*")
                // 是否允许携带认证信息（Cookie、Authorization Header）
                .allowCredentials(true)
                // 预检请求（OPTIONS）的缓存时间（秒），减少预检请求次数
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     * <p>
     * 用于在 Controller 方法执行前后的通用处理，
     * 如登录校验、权限校验、接口耗时统计等。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 示例：登录拦截器
        // registry.addInterceptor(new LoginInterceptor())
        //         .addPathPatterns("/api/**")  // 拦截路径
        //         .excludePathPatterns("/api/login", "/api/public/**")  // 排除路径
        //         .order(1);  // 执行顺序，数字越小优先级越高
    }

    /**
     * 添加自定义参数解析器
     * <p>
     * 将请求参数自动绑定到 Controller 方法参数，
     * 如 @CurrentUser 注解自动注入当前登录用户。
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 后台用户参数解析器（@CurrentUser 注解）
        resolvers.add(new CurrentUserArgumentResolver());
    }

    /**
     * 添加格式化器和转换器
     * <p>
     * 注册 Spring Converter，用于类型转换和格式化。
     *
     * @param registry 格式化器注册表
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 枚举转换器（将请求参数中的枚举值转换为 Java 枚举）
        registry.addConverterFactory(new EnumConverterFactory());
    }

}
