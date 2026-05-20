package com.xiyao.security.config;

import com.xiyao.common.utils.RedisUtils;
import com.xiyao.security.filter.JwtAuthenticationFilter;
import com.xiyao.security.handler.AccessDeniedHandlerImpl;
import com.xiyao.security.handler.AuthenticationEntryPointImpl;
import com.xiyao.security.properties.SecurityData;
import com.xiyao.security.utils.JwtUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityData.class)
@ConditionalOnProperty(value = "security-data.enabled", havingValue = "true")
public class SecurityConfig {

    /**
     * 认证工具类
     */
    @Bean
    public JwtUtils jwtUtils(RedisUtils redisUtils, SecurityData properties) {
        return new JwtUtils(redisUtils, properties);
    }

    /**
     * 认证管理器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils) {
        return new JwtAuthenticationFilter(jwtUtils);
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置 Spring Security 过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, SecurityData properties) throws Exception {
        return http
                // 禁用 CSRF（前后端分离无状态应用）
                .csrf(AbstractHttpConfigurer::disable)
                // 启用 CORS 会读取 WebMvcConfig 的跨域配置
                .cors(Customizer.withDefaults())
                // 设置 Session 为无状态（不使用 Session 存储上下文）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 认证失败处理类
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new AuthenticationEntryPointImpl())
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )
                // 请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口(无需认证)
                        .requestMatchers(properties.getIncludePaths().toArray(String[]::new)).permitAll()
                        // 放行静态资源(如文档等，按需添加)
                        .requestMatchers(properties.getStaticPaths().toArray(String[]::new)).permitAll()
                        // 其他任何请求都需要认证
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器(UsernamePasswordAuthenticationFilter 之前执行)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}