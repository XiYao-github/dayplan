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
 */
@Configuration
@EnableWebMvc  // 完全接管Spring MVC配置（若不需要可注释，使用默认配置）
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许跨域的路径
                .allowedOriginPatterns("*")  // 允许跨域的来源，*表示所有，生产环境建议指定具体域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")  // 允许的请求方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 是否允许携带cookie
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }

    /**
     * 静态资源配置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 默认静态资源映射（Spring Boot默认已配置，这里仅展示）
        // registry.addResourceHandler("/**")
        //         .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/resources/", "classpath:/META-INF/resources/");

        // 自定义静态资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/path/to/uploads/")  // 外部文件目录
                .setCachePeriod(3600)  // 缓存时间（秒）
                .resourceChain(true);  // 开启资源链

        // Swagger UI静态资源（如果使用）
        // registry.addResourceHandler("/swagger-ui/**")
        //         .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");

        // 可选：配置缓存控制
        // registry.addResourceHandler("/assets/**")
        //         .addResourceLocations("classpath:/static/assets/")
        //         .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
        //                 .cachePublic()
        //                 .mustRevalidate());
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 示例：登录拦截器
        // registry.addInterceptor(new LoginInterceptor())
        //         .addPathPatterns("/api/**")  // 拦截路径
        //         .excludePathPatterns("/api/login", "/api/register", "/api/public/**")  // 排除路径
        //         .order(1);  // 优先级

        // 示例：权限拦截器
        // registry.addInterceptor(new PermissionInterceptor())
        //         .addPathPatterns("/api/admin/**")
        //         .order(2);

        // 示例：日志拦截器
        // registry.addInterceptor(new LogInterceptor())
        //         .addPathPatterns("/**")
        //         .order(0);
    }

    /**
     * 视图控制器（无业务逻辑的简单页面跳转）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // registry.addViewController("/").setViewName("index");
        // registry.addViewController("/login").setViewName("login");
        // registry.addViewController("/error/404").setViewName("error/404");
    }

    /**
     * 路径匹配配置
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 设置是否使用后缀模式匹配（默认true，建议false避免安全隐患）
        configurer.setUseSuffixPatternMatch(false);

        // 设置是否使用尾斜杠匹配（默认true）
        configurer.setUseTrailingSlashMatch(true);

        // 设置路径匹配器（使用PathPatternParser性能更好）
        // configurer.setPathMatcher(new AntPathMatcher());

        // 设置URL路径前缀
        // configurer.addPathPrefix("/api", HandlerTypePredicate.forBasePackageType("com.example.demo.controller"));
    }

    /**
     * 自定义参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 后台用户参数解析器(@CurrentUser)
        resolvers.add(new CurrentUserArgumentResolver());
        // resolvers.add(new HandlerMethodArgumentResolver() {
        //     @Override
        //     public boolean supportsParameter(MethodParameter parameter) {
        //         return parameter.hasParameterAnnotation(CurrentUser.class) && parameter.getParameterType().isAssignableFrom(LoginUser.class);
        //     }
        //
        //     @Override
        //     public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //         return SecurityUtils.getLoginUser();
        //     }
        // });

        // resolvers.add(new PageableArgumentResolver());
    }

    /**
     * 自定义返回值处理器
     */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        // handlers.add(new CustomReturnValueHandler());
    }

    /**
     * 消息转换器配置
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 默认会添加多个转换器，这里可以自定义
        // 如果需要使用自定义ObjectMapper，可以在这里配置
        // MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        // jacksonConverter.setObjectMapper(customObjectMapper());
        // converters.add(0, jacksonConverter);
    }

    /**
     * 扩展消息转换器（保留默认转换器）
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 在现有转换器基础上扩展
        // 例如：添加FastJson转换器
        // FastJsonHttpMessageConverter fastJsonConverter = new FastJsonHttpMessageConverter();
        // converters.add(fastJsonConverter);
    }

    /**
     * 格式化器配置
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // registry.addFormatter(new DateFormatter("yyyy-MM-dd HH:mm:ss"));
        // registry.addConverter(new StringToDateConverter());
    }

    /**
     * 异步请求配置
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000)  // 默认超时时间（毫秒）
                .setTaskExecutor(new ThreadPoolTaskExecutor() {
                    {
                        setCorePoolSize(10);
                        setMaxPoolSize(50);
                        setQueueCapacity(100);
                    }
                });  // 自定义异步任务执行器

        // 异步请求拦截器
        // configurer.registerCallableInterceptors(new TimeoutCallableProcessingInterceptor());
        // configurer.registerDeferredResultInterceptors(new TimeoutDeferredResultProcessingInterceptor());
    }

    /**
     * 内容协商配置
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
