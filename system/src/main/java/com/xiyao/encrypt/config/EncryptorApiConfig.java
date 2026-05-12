package com.xiyao.encrypt.config;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.encrypt.filter.EncryptorFilter;
import com.xiyao.encrypt.properties.EncryptorApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Objects;

/**
 * 接口加解密配置
 */
@Configuration
@EnableConfigurationProperties(EncryptorApi.class)
@ConditionalOnProperty(value = "encryptor-api.enabled", havingValue = "true")
public class EncryptorApiConfig {

    @Bean
    public FilterRegistrationBean<EncryptorFilter> filterFilterRegistrationBean(EncryptorApi properties) {
        // 校验配置
        Objects.requireNonNull(properties.getHeaderFlag(), "加解密头部标识不能为空");
        Objects.requireNonNull(properties.getPublicKey(), "响应加密公钥不能为空");
        Objects.requireNonNull(properties.getPrivateKey(), "请求解密私钥不能为空");
        // 创建 Filter
        EncryptorFilter filter = new EncryptorFilter(properties);
        // 注册 Filter
        FilterRegistrationBean<EncryptorFilter> registration = new FilterRegistrationBean<>();
        // 注册过滤器
        registration.setFilter(filter);
        // 设置 URL 路径
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
