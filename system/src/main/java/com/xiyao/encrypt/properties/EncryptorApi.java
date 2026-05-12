package com.xiyao.encrypt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 配置文件加解密属性
 */
@Data
@ConfigurationProperties(prefix = "encryptor-api")
public class EncryptorApi {

    /**
     * 是否开启加密
     */
    private Boolean enabled = false;

    /**
     * 加密头标识
     */
    private String headerFlag = "headerFlag";

    /**
     * 响应加密公钥
     */
    private String publicKey;

    /**
     * 请求解密私钥
     */
    private String privateKey;

    /**
     * 包含加解密路径(默认 "/*")
     */
    private List<String> includePaths = new ArrayList<>(Collections.singletonList("/*"));
    /**
     * 排除加解密路径
     */
    private List<String> excludePaths = new ArrayList<>();

}
