package com.xiyao.crypto.core;

import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;
import lombok.Data;

/**
 * 加解密上下文配置
 * <p>
 * 封装加解密操作所需的配置参数，包括算法类型、编码方式、密钥信息等。
 * 通过 EncryptorManager 在加密/解密时传递配置，支持字段注解级别覆盖全局配置。
 *
 * <p>
 * <b>配置优先级：</b>
 * <ul>
 *     <li>字段注解属性（&gt; 0）：使用字段注解配置的值</li>
 *     <li>字段注解属性（= DEFAULT）：使用全局配置的值</li>
 * </ul>
 *
 * @author xiyao
 * @see AlgorithmType
 * @see EncodeType
 */
@Data
public class EncryptContext {

    /**
     * 加密算法类型
     * <p>
     * 支持 SM2/SM4 等国密算法。
     */
    private AlgorithmType algorithm;

    /**
     * 加密后的编码格式
     * <p>
     * 指定密文输出格式：Base64 或 Hex。
     */
    private EncodeType encode;

    /**
     * 对称加密密钥
     * <p>
     * SM4 算法使用的 16 位密钥字符串。
     */
    private String password;

    /**
     * SM2 公钥
     * <p>
     * 用于 SM2 公钥加密模式。
     */
    private String publicKey;

    /**
     * SM2 私钥
     * <p>
     * 用于 SM2 私钥解密模式。
     */
    private String privateKey;
}
