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
 * <p>
 * 功能：
 * <ol>
 *     <li>配置 JWT 认证过滤器（无状态认证）</li>
 *     <li>配置请求授权规则（放行登录接口，认证其他请求）</li>
 *     <li>配置认证/授权失败处理器</li>
 *     <li>配置密码加密器（BCrypt）</li>
 * </ol>
 *
 * <p>
 * <b>认证流程：</b>
 * <ol>
 *     <li>请求进入 JwtAuthenticationFilter</li>
 *     <li>从 Header/参数中提取 JWT Token</li>
 *     <li>验证 Token 有效性，获取用户信息</li>
 *     <li>设置 SecurityContext 用户认证信息</li>
 *     <li>后续请求通过 SecurityContext 获取当前用户</li>
 * </ol>
 *
 * @author xiyao
 */
@Configuration
@EnableMethodSecurity  // 启用方法级安全注解（如 @PreAuthorize）
@EnableConfigurationProperties(SecurityData.class)  // 启用配置属性类
@ConditionalOnProperty(value = "security-data.enabled", havingValue = "true")
public class SecurityConfig {

    /**
     * JWT 工具类
     *
     * @param redisUtils Redis 工具类（用于 Token 黑名单）
     * @param properties Security 配置属性
     * @return JwtUtils 实例
     */
    @Bean
    public JwtUtils jwtUtils(RedisUtils redisUtils, SecurityData properties) {
        return new JwtUtils(redisUtils, properties);
    }

    /**
     * JWT 认证过滤器
     *
     * @param jwtUtils JWT 工具类
     * @return JwtAuthenticationFilter 实例
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils) {
        return new JwtAuthenticationFilter(jwtUtils);
    }

    /**
     * 密码编码器
     * <p>
     * 使用 BCrypt 算法加密密码，
     * 同明文每次加密结果不同，但可通过 BCryptPasswordEncoder 验证。
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * <p>
     * 用于处理认证请求，如登录认证。
     *
     * @param config 认证配置
     * @return AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置 Spring Security 过滤器链
     * <p>
     * 定义请求授权规则、Session 策略、异常处理等。
     *
     * @param http         HttpSecurity 配置Builder
     * @param jwtFilter     JWT 认证过滤器
     * @param properties   Security 配置属性
     * @return SecurityFilterChain 实例
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, SecurityData properties) throws Exception {
        return http
                // ========== 基础安全配置 ==========

                // 禁用 CSRF（前后端分离无状态应用，Token 本身已具备防 CSRF 能力）
                .csrf(AbstractHttpConfigurer::disable)

                // 启用 CORS（跨域资源共享）
                // 配置来源于 WebMvcConfig 中定义的跨域配置
                .cors(Customizer.withDefaults())

                // 设置 Session 为无状态（不创建 Session，使用 JWT Token 认证）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========== 异常处理 ==========

                // 认证失败处理（未登录或 Token 无效）
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new AuthenticationEntryPointImpl())
                        // 权限不足处理（已登录但权限不足）
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )

                // ========== 请求授权 ==========

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/audit/**").hasRole("AUDIT_ADMIN")
                        .requestMatchers("/user/**", "/role/**").hasAnyRole("SYSTEM_ADMIN", "SECURITY_ADMIN")
                        .requestMatchers("/config/**").hasRole("SYSTEM_ADMIN")
                        // 放行登录接口（无需认证即可访问）
                        .requestMatchers(properties.getIncludePaths().toArray(String[]::new)).permitAll()
                        // 放行静态资源路径
                        .requestMatchers(properties.getStaticPaths().toArray(String[]::new)).permitAll()
                        // 其他任何请求都需要认证
                        .anyRequest().authenticated()
                )

                // ========== Filter 配置 ==========

                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}