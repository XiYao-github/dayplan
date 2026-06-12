package com.xiyao.security.config;

import com.xiyao.framework.utils.RedisUtils;
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
 * 配置 JWT 认证过滤器、请求授权规则、异常处理、密码加密器等核心安全组件。
 *
 * <p>
 * <b>主要功能：</b>
 * <ul>
 *     <li>配置 JWT 认证过滤器，实现无状态认证</li>
 *     <li>配置请求授权规则，放行无需认证的接口</li>
 *     <li>配置认证失败和权限不足的处理器</li>
 *     <li>配置密码加密器（BCrypt）</li>
 *     <li>启用方法级安全注解（@PreAuthorize）</li>
 * </ul>
 *
 * <p>
 * <b>认证流程：</b>
 * <ol>
 *     <li>请求进入 JwtAuthenticationFilter 过滤器</li>
 *     <li>从 Header/参数中提取 JWT Token</li>
 *     <li>验证 Token 有效性（签名、过期时间）</li>
 *     <li>从 Redis 获取用户信息，设置 SecurityContext</li>
 *     <li>后续请求通过 SecurityContext 获取当前用户</li>
 * </ol>
 *
 * <p>
 * <b>配置条件：</b>
 * 只有当 security-data.enable=true 时才加载此配置类。
 * 可以通过设置 enable=false 来禁用安全配置（测试环境）。
 *
 * @author xiyao
 * @see JwtAuthenticationFilter
 * @see JwtUtils
 */
@Configuration
@EnableMethodSecurity  // 启用方法级安全注解（如 @PreAuthorize、@Secured）
@EnableConfigurationProperties(SecurityData.class)  // 启用配置属性类，使 @ConfigurationProperties 生效
@ConditionalOnProperty(value = "system.security.enable", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    // ==================== Bean 定义 ====================

    /**
     * 创建 JwtUtils 实例
     * <p>
     * JwtUtils 依赖 RedisUtils 和 SecurityData 配置，
     * 需要通过 @Bean 方式注入。
     *
     * @param redisUtils  Redis 工具类（用于 Token 缓存）
     * @param properties  Security 配置属性（包含 JWT 密钥和过期时间）
     * @return JwtUtils 实例
     */
    @Bean
    public JwtUtils jwtUtils(RedisUtils redisUtils, SecurityData properties) {
        // 通过构造器注入依赖
        return new JwtUtils(redisUtils, properties);
    }

    /**
     * 创建 JWT 认证过滤器
     * <p>
     * 此过滤器在每个请求进入时执行，负责解析 Token 和设置用户认证信息。
     *
     * @param jwtUtils JWT 工具类
     * @return JwtAuthenticationFilter 实例
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils) {
        // 创建过滤器实例
        return new JwtAuthenticationFilter(jwtUtils);
    }

    /**
     * 创建密码编码器
     * <p>
     * 使用 BCrypt 算法加密密码。
     * BCrypt 是单向哈希算法，每次加密结果不同，但验证时可匹配。
     * 内置盐值生成，安全性高，适合密码存储。
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 创建 BCrypt 密码编码器
        // BCrypt 会自动生成随机盐，每次 encode 结果不同
        // matches() 方法会自动提取盐值进行比对
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建认证管理器
     * <p>
     * AuthenticationManager 是 Spring Security 的核心组件，
     * 用于处理认证请求（如登录时的用户名密码验证）。
     *
     * <p>
     * <b>使用场景：</b>
     * <ul>
     *     <li>登录时调用 authenticate() 验证用户名密码</li>
     *     <li>调用 UserDetailsServiceImpl 加载用户信息</li>
     * </ul>
     *
     * @param config 认证配置（包含 AuthenticationManager 实例）
     * @return AuthenticationManager 实例
     * @throws Exception 获取认证管理器失败时抛出
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // 从配置中获取 AuthenticationManager 实例
        return config.getAuthenticationManager();
    }

    // ==================== SecurityFilterChain 配置 ====================

    /**
     * 配置 Spring Security 过滤器链
     * <p>
     * 这是 Security 配置的核心方法，定义：
     * <ul>
     *     <li>基础安全配置（CSRF、CORS、Session）</li>
     *     <li>异常处理（认证失败、权限不足）</li>
     *     <li>请求授权规则（哪些路径需要什么权限）</li>
     *     <li>过滤器配置（JWT 过滤器位置）</li>
     * </ul>
     *
     * <p>
     * <b>请求授权规则：</b>
     * <ul>
     *     <li>/audit/**：需要 AUDIT_ADMIN 角色（审计管理员）</li>
     *     <li>/user/**, /role/**：需要 SYSTEM_ADMIN 或 SECURITY_ADMIN 角色</li>
     *     <li>/config/**：需要 SYSTEM_ADMIN 角色</li>
     *     <li>includePaths 配置的路径：放行，无需认证</li>
     *     <li>staticPaths 配置的静态资源：放行</li>
     *     <li>其他请求：需要认证</li>
     * </ul>
     *
     * @param http       HttpSecurity 配置 Builder
     * @param jwtFilter  JWT 认证过滤器
     * @param properties Security 配置属性（包含放行路径等）
     * @return SecurityFilterChain 过滤器链实例
     * @throws Exception 配置过程中出错时抛出
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, SecurityData properties) throws Exception {
        return http
                // ========== 基础安全配置 ==========

                // 禁用 CSRF 防护
                // 原因：前后端分离应用使用 JWT Token 认证，Token 本身已具备防 CSRF 能力
                //       传统基于 Session 的 CSRF 防护不适用于无状态架构
                .csrf(AbstractHttpConfigurer::disable)

                // 启用 CORS（跨域资源共享）
                // 跨域配置来源于 WebMvcConfig 中定义的 CorsConfigurationSource Bean
                .cors(Customizer.withDefaults())

                // 配置 Session 管理策略为无状态
                // 原因：使用 JWT Token 认证，不需要在服务端存储 Session
                // STATELESS 模式下 Spring Security 不会创建 HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========== 异常处理 ==========

                // 配置认证和授权异常的处理策略
                .exceptionHandling(exception -> exception
                        // 认证失败入口点（用户未登录或 Token 无效）
                        // 触发场景：用户未登录直接访问受保护资源
                        .authenticationEntryPoint(new AuthenticationEntryPointImpl())
                        // 权限不足处理（用户已登录但权限不够）
                        // 触发场景：用户已登录但访问需要更高权限的接口
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )

                // ========== 请求授权 ==========

                .authorizeHttpRequests(auth -> auth
                        // 放行无需认证的接口（如登录、注册、验证码等）
                        // 路径列表来自配置文件 security-data.include-paths
                        .requestMatchers(properties.getIncludePaths().toArray(String[]::new)).permitAll()
                        // 放行静态资源（CSS、JS、图片等）
                        .requestMatchers(properties.getStaticPaths().toArray(String[]::new)).permitAll()
                        // 其他所有请求都需要认证才能访问
                        .anyRequest().authenticated()
                )

                // ========== 过滤器配置 ==========

                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                // 确保 Token 认证在表单登录认证之前执行
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 构建并返回过滤器链
                .build();
    }
}