package com.xiyao.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Security 安全模块配置属性
 * <p>
 * 通过 application.yml 配置文件配置安全相关的参数，
 * 包括放行路径、JWT 配置等。
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   security:
 *     enable: true
 *     include-paths:
 *       - /login
 *       - /register
 *       - /code
 *     static-paths:
 *       - /static/**
 *       - /public/**
 *     jwt:
 *       secret: your-secret-key-here
 *       expire: 7200
 * }</pre>
 *
 * <p>
 * <b>配置项说明：</b>
 * <ul>
 *     <li>enable：是否启用安全过滤，false 时跳过所有安全配置</li>
 *     <li>include-paths：无需认证即可访问的路径列表</li>
 *     <li>static-paths：静态资源路径（CSS、JS、图片等）</li>
 *     <li>jwt.secret：JWT 签名密钥，建议使用复杂字符串</li>
 *     <li>jwt.expire：Token 过期时间（秒），默认 7200（2小时）</li>
 * </ul>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "system.security")
public class SecurityData {

    /**
     * 是否启用安全过滤
     * <p>
     * 设为 true 时启用 Security 安全过滤，
     * 设为 false 时跳过所有安全配置（用于测试环境）。
     */
    private Boolean enable = false;

    /**
     * 放行路径列表
     * <p>
     * 配置无需认证即可访问的接口路径，
     * 支持 Ant 风格路径表达式（如 /api/**）。
     * 通常配置登录接口、静态资源接口等。
     */
    private List<String> includePaths = new ArrayList<>();

    /**
     * 静态资源路径列表
     * <p>
     * 配置静态资源的访问路径，
     * 这些路径不需要认证即可访问。
     * 支持 Ant 风格路径表达式。
     */
    private List<String> staticPaths = new ArrayList<>();

    /**
     * JWT 配置
     * <p>
     * 包含 JWT Token 生成和验证所需的配置参数。
     */
    private JwtData jwt;

    /**
     * JWT 配置内部类
     * <p>
     * 定义 JWT 相关的配置项。
     */
    @Data
    public static class JwtData {

        /**
         * JWT 签名密钥
         * <p>
         * 用于生成和验证 JWT Token 的签名。
         * 建议使用复杂、足够长度的字符串作为密钥。
         */
        private String secret;

        /**
         * Token 过期时间
         * <p>
         * 单位为秒，默认 7200 秒（2小时）。
         * 过期后用户需要重新登录获取新 Token。
         */
        private Long expire;
    }
}