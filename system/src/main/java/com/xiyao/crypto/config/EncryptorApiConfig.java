package com.xiyao.crypto.config;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.crypto.filter.EncryptorFilter;
import com.xiyao.crypto.properties.EncryptorApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Objects;

/**
 * 接口加解密配置类
 * <p>
 * 配置基于 Filter 的请求/响应加解密过滤器。
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>启用条件：encryptor-api.enable=true</li>
 *     <li>注册 EncryptorFilter 过滤器，对指定路径的请求进行解密、响应进行加密</li>
 *     <li>使用 SM2 + SM4 混合加密模式：SM2 加密随机 SM4 密钥，SM4 加密实际数据</li>
 * </ul>
 * <p>
 * <b>加密流程：</b>
 * <ol>
 *     <li>客户端生成随机 SM4 密钥，用 SM2 公钥加密后放在请求头</li>
 *     <li>客户端用 SM4 密钥加密请求体，发送到服务端</li>
 *     <li>Filter 用 SM2 私钥解密出 SM4 密钥，再用 SM4 解密请求体</li>
 *     <li>响应时用同样的 SM4 密钥加密响应体，返回给客户端</li>
 * </ol>
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * encryptor-api:
 *   enable: true
 *   headerFlag: "encrypt-data"
 *   publicKey: "sm2-public-key-for-response"
 *   privateKey: "sm2-private-key-for-request"
 *   includePaths:
 *     - "/api/*"
 *   excludePaths:
 *     - "/api/public/*"
 * }</pre>
 *
 * @author xiyao
 * @see EncryptorFilter
 * @see EncryptorApi
 */
@Configuration
@EnableConfigurationProperties(EncryptorApi.class)
@ConditionalOnProperty(value = "crypto-api.enable", havingValue = "true")
public class EncryptorApiConfig {

    /**
     * 注册加密过滤器
     * <p>
     * 将 EncryptorFilter 注册到 Spring MVC 过滤器链中，配置 URL 路径匹配规则和执行顺序。
     *
     * @param properties 配置属性（通过 @ConfigurationProperties 绑定）
     * @return FilterRegistrationBean 过滤器注册Bean
     * @throws NullPointerException 如果必需的配置文件为空
     */
    @Bean
    public FilterRegistrationBean<EncryptorFilter> filterFilterRegistrationBean(EncryptorApi properties) {
        // 校验配置：头部标识不能为空
        Objects.requireNonNull(properties.getHeaderFlag(), "加解密头部标识不能为空");
        // 校验配置：响应加密公钥不能为空
        Objects.requireNonNull(properties.getPublicKey(), "响应加密公钥不能为空");
        // 校验配置：请求解密私钥不能为空
        Objects.requireNonNull(properties.getPrivateKey(), "请求解密私钥不能为空");

        // 创建 Filter 实例
        EncryptorFilter filter = new EncryptorFilter(properties);

        // 注册 Filter
        FilterRegistrationBean<EncryptorFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);

        // 配置 URL 路径匹配模式
        List<String> patterns = properties.getIncludePaths();
        if (ObjectUtil.isEmpty(patterns)) {
            // 默认拦截所有请求
            registration.addUrlPatterns("/*");
        } else {
            registration.setUrlPatterns(patterns);
        }

        // 设置执行顺序为最高优先级，确保过滤器最早执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}