package com.xiyao.crypto.properties;

import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据加解密配置属性
 * <p>
 * 绑定配置前缀 encryptor-data，用于配置基于 MyBatis 拦截器的字段加解密功能。
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   crypto:
 *     data:
 *       enable: true                          # 是否开启字段加解密
 *       algorithm: SM4                        # 默认加密算法
 *       encode: HEX                           # 默认编码方式
 *       password: "your-16-char-key"         # SM4 密钥（16字符）
 *       publicKey: "sm2-public-key"           # SM2 公钥
 *       privateKey: "sm2-private-key"         # SM2 私钥
 * }</pre>
 * <p>
 * <b>配置说明：</b>
 * <ul>
 *     <li>enable：true 时启用 EncryptInterceptor 和 DecryptInterceptor</li>
 *     <li>algorithm：默认加密算法，可被字段注解覆盖</li>
 *     <li>encode：默认编码方式，可被字段注解覆盖</li>
 *     <li>password/keys：密钥配置，支持 SM4 和 SM2</li>
 * </ul>
 * <p>
 * <b>优先级规则：</b>
 * 字段注解配置 > 全局配置。即如果字段指定了 algorithm，则使用字段指定的，否则使用全局配置的。
 *
 * @author xiyao
 * @see AlgorithmType
 * @see EncodeType
 */
@Data
@ConfigurationProperties(prefix = "system.crypto.data")
public class EncryptorData {

    /**
     * 是否开启字段加解密
     * <p>
     * 当设置为 true 时，系统会注册 EncryptInterceptor 和 DecryptInterceptor
     * 对带有 @CryptoField 注解的字段进行加解密处理。
     *
     * @return true 表示启用，false 表示禁用
     */
    private Boolean enable = true;

    /**
     * 默认加密算法
     * <p>
     * 当字段注解未指定算法时，使用此配置。
     *
     * @return 加密算法类型
     */
    private AlgorithmType algorithm = AlgorithmType.SM4;

    /**
     * 默认编码方式
     * <p>
     * 当字段注解未指定编码方式时，使用此配置。
     *
     * @return 编码类型
     */
    private EncodeType encode = EncodeType.HEX;

    /**
     * SM4 对称加密密钥
     * <p>
     * 用于 SM4 算法的加密和解密。建议使用 16 位随机字符串。
     * 当字段注解未指定密钥时，使用此配置。
     *
     * @return SM4 密钥
     */
    private String password;

    /**
     * SM2 非对称加密公钥
     * <p>
     * 用于 SM2 算法的加密（公钥加密，私钥解密）。
     * 当字段注解未指定公钥时，使用此配置。
     *
     * @return SM2 公钥
     */
    private String publicKey;

    /**
     * SM2 非对称加密私钥
     * <p>
     * 用于 SM2 算法的解密。
     * 当字段注解未指定私钥时，使用此配置。
     *
     * @return SM2 私钥
     */
    private String privateKey;

}