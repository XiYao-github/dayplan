package com.xiyao.encrypt.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.encrypt.core.EncryptorManager;
import com.xiyao.encrypt.interceptor.DecryptInterceptor;
import com.xiyao.encrypt.interceptor.EncryptInterceptor;
import com.xiyao.encrypt.properties.EncryptorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 数据加解密配置
 */
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorData.class)
@ConditionalOnProperty(value = "encryptor-data.enable", havingValue = "true")
public class EncryptorDataAutoConfiguration {

    @Autowired
    private EncryptorData properties;

    @Bean
    public EncryptorManager encryptorManager() {
        return new EncryptorManager();
    }

    @Bean
    public EncryptInterceptor mybatisEncryptInterceptor(EncryptorManager encryptorManager) {
        return new EncryptInterceptor(encryptorManager, properties);
    }

    @Bean
    public DecryptInterceptor mybatisDecryptInterceptor(EncryptorManager encryptorManager) {
        return new DecryptInterceptor(encryptorManager, properties);
    }

}



