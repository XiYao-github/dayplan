package com.xiyao.framework.config;

import com.xiyao.framework.converter.MyEnumConverterFactory;
import com.xiyao.framework.interceptor.DecryptInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 **/
@Configuration
public class MyWebConfig implements WebMvcConfigurer {


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new MyEnumConverterFactory());
    }

    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     registry.addInterceptor(new DecryptInterceptor());
    // }

    /**
     * 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping("/**")
                // 置允许跨域请求的域名
                .allowedOriginPatterns("*")
                //是否允许证书（cookies）
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //.allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
