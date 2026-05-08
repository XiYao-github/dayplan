package com.xiyao.encrypt.properties;

import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件加解密属性
 */
@Data
@ConfigurationProperties(prefix = "encryptor-data")
public class EncryptorData {

    /**
     * 是否开启加密
     */
    private Boolean enable;

    /**
     * 加密算法
     */
    private AlgorithmType algorithm;

    /**
     * 编码方式
     */
    private EncodeType encode;

    /**
     * 密钥(对称加密算法)
     */
    private String password;

    /**
     * 公钥(非对称加密算法)
     */
    private String publicKey;

    /**
     * 私钥(非对称加密算法)
     */
    private String privateKey;

}
