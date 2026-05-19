package com.xiyao.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件加解密属性
 */
@Data
@ConfigurationProperties(prefix = "security-data")
public class SecurityData {

    /**
     * 是否开启权限过滤
     */
    private Boolean enabled = false;

    /**
     * 放行路径
     */
    private List<String> includePaths = new ArrayList<>();

    /**
     * 静态路径
     */
    private List<String> staticPaths = new ArrayList<>();

    /**
     * JWT 配置
     */
    private JwtData jwt;

    @Data
    public static class JwtData {

        /**
         * 密钥
         */
        private String secret;

        /**
         * 过期时间(秒)
         */
        private Long expire;
    }
}
