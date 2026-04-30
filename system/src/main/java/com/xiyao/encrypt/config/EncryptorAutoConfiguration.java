package com.xiyao.encrypt.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.xiyao.encrypt.core.EncryptorManager;
import com.xiyao.encrypt.interceptor.DecryptInterceptor;
import com.xiyao.encrypt.interceptor.EncryptInterceptor;
import com.xiyao.encrypt.properties.EncryptorProperties;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加解密配置
 */
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnProperty(value = "mybatis-encryptor.enable", havingValue = "true")
@Slf4j
public class EncryptorAutoConfiguration {

    @Autowired
    private EncryptorProperties properties;

    @Bean
    public EncryptorManager encryptorManager(MybatisPlusProperties mybatisPlusProperties) {
        return new EncryptorManager(mybatisPlusProperties.getTypeAliasesPackage());
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



