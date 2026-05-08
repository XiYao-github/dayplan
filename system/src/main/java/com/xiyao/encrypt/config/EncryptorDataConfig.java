package com.xiyao.encrypt.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.encrypt.core.EncryptorManager;
import com.xiyao.encrypt.interceptor.DecryptInterceptor;
import com.xiyao.encrypt.interceptor.EncryptInterceptor;
import com.xiyao.encrypt.properties.EncryptorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据加解密配置
 */
// @AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorData.class)
@ConditionalOnProperty(value = "encryptor-data.enable", havingValue = "true")
public class EncryptorDataConfig {

    @Autowired
    private EncryptorData properties;

    @Bean
    public EncryptorManager encryptorManager() {
        return new EncryptorManager();
    }

    @Bean
    public EncryptInterceptor encryptInterceptor(EncryptorManager encryptorManager) {
        return new EncryptInterceptor(encryptorManager, properties);
    }

    @Bean
    public DecryptInterceptor decryptInterceptor(EncryptorManager encryptorManager) {
        return new DecryptInterceptor(encryptorManager, properties);
    }

}



