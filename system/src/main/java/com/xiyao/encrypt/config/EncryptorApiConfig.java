package com.xiyao.encrypt.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.filter.EncryptorFilter;
import com.xiyao.encrypt.properties.EncryptorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * 接口加解密配置
 */
// @AutoConfiguration
@Configuration
@EnableConfigurationProperties(EncryptorApi.class)
@ConditionalOnProperty(value = "encryptor-api.enabled", havingValue = "true")
public class EncryptorApiConfig {

    @Autowired
    private EncryptorApi properties;

    @Bean
    public EncryptorFilter encryptorFilter() {
        if (StrUtil.isBlank(properties.getHeaderFlag())) {
            throw new IllegalArgumentException("加解密头部标识不能为空");
        }
        if (StrUtil.isBlank(properties.getPublicKey())) {
            throw new IllegalArgumentException("响应加密公钥不能为空");
        }
        if (StrUtil.isBlank(properties.getPrivateKey())) {
            throw new IllegalArgumentException("请求解密私钥不能为空");
        }
        return new EncryptorFilter(properties);
    }

    @Bean
    public FilterRegistrationBean<EncryptorFilter> filterFilterRegistrationBean(EncryptorFilter cryptoFilter) {
        FilterRegistrationBean<EncryptorFilter> registration = new FilterRegistrationBean<>();
        // 注册过滤器
        registration.setFilter(cryptoFilter);
        // 设置 URL
        List<String> patterns = properties.getIncludePaths();
        if (ObjectUtil.isEmpty(patterns)) {
            registration.addUrlPatterns("/*");
        } else {
            registration.setUrlPatterns(patterns);
        }
        // 设置执行顺序，数值越小越先执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
