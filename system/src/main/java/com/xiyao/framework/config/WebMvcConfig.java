package com.xiyao.framework.config;

import com.xiyao.framework.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * Web MVC 配置类
 * <p>
 * 配置 Spring MVC 的各项组件，包括：
 * <ul>
 *     <li>跨域资源共享（CORS）配置</li>
 *     <li>静态资源处理</li>
 *     <li>拦截器注册</li>
 *     <li>参数解析器和返回值处理器</li>
 *     <li>消息转换器和格式化器</li>
 *     <li>异步请求处理</li>
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
     * 配置静态资源处理
     * <p>
     * 将 URL 路径映射到实际文件资源。
     *
     * @param registry 静态资源注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 自定义静态资源映射示例：外部文件目录
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:/path/to/uploads/")  // 外部文件目录
        //         .setCachePeriod(3600)  // 缓存时间（秒）
        //         .resourceChain(true);  // 开启资源链（支持缓存、 gzip 压缩等）
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
     * 配置视图控制器
     * <p>
     * 用于无业务逻辑的简单页面跳转，直接将 URL 映射到视图模板。
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 示例：根路径跳转到 index 页面
        // registry.addViewController("/").setViewName("index");
        // registry.addViewController("/login").setViewName("login");
    }

    /**
     * 配置路径匹配规则
     * <p>
     * 控制 URL 路径的匹配方式。
     *
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 禁用后缀模式匹配（如 /user.do 匹配 /user），防止安全风险
        configurer.setUseSuffixPatternMatch(false);
        // 启用尾斜杠匹配（如 /user 和 /user/ 等效）
        configurer.setUseTrailingSlashMatch(true);
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
     * 添加自定义返回值处理器
     * <p>
     * 处理 Controller 返回值的转换和包装。
     *
     * @param handlers 返回值处理器列表
     */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        // 可添加自定义返回值处理器
    }

    /**
     * 配置消息转换器
     * <p>
     * 添加或替换 Spring MVC 用于转换请求/响应体的转换器。
     * 此方法会替换默认转换器列表。
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 默认会添加多个转换器，这里可以完全替换
        // MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        // jacksonConverter.setObjectMapper(customObjectMapper());
        // converters.add(0, jacksonConverter);
    }

    /**
     * 扩展消息转换器
     * <p>
     * 在现有转换器列表基础上添加新的转换器，保留默认转换器。
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 在现有转换器基础上扩展，如添加 FastJson 转换器
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
        // 数据字典枚举转换器（将请求参数中的字典值转换为 Java 枚举）
        // registry.addConverterFactory(new EnumConverterFactory());
    }

    /**
     * 配置异步请求处理
     * <p>
     * 配置异步请求的超时时间和执行线程池。
     *
     * @param configurer 异步支持配置器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 异步请求默认超时时间（毫秒）
        configurer.setDefaultTimeout(30000);
        // 自定义异步任务执行线程池
        configurer.setTaskExecutor(new ThreadPoolTaskExecutor() {
            {
                setCorePoolSize(10);
                setMaxPoolSize(50);
                setQueueCapacity(100);
            }
        });
    }

    /**
     * 配置内容协商
     * <p>
     * 决定如何根据请求选择合适的响应格式（如 JSON、XML）。
     *
     * @param configurer 内容协商配置器
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 基于请求参数的内容协商（默认关闭）
        // configurer.favorParameter(true)
        //         .parameterName("format")
        //         .ignoreAcceptHeader(false)
        //         .defaultContentType(MediaType.APPLICATION_JSON)
        //         .mediaType("json", MediaType.APPLICATION_JSON)
        //         .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
