package com.xiyao.encrypt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件加解密属性
 */
@Data
@ConfigurationProperties(prefix = "encryptor-data")
public class EncryptorData {

    /**
     * 过滤开关
     */
    private Boolean enable;

    /**
     * 安全秘钥
     */
    private String password;

}
