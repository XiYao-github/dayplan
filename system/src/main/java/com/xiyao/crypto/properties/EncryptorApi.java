package com.xiyao.crypto.properties;

import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 接口加解密配置属性
 * <p>
 * 绑定配置前缀 encryptor-api，用于配置基于 Filter 的请求/响应加解密功能。
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   crypto:
 *     api:
 *       enable: true                                    # 是否开启接口加解密
 *       headerFlag: "encrypt-data"                      # 请求头标识，用于传递 SM4 密钥
 *       publicKey: "sm2-public-key-for-response"       # 响应加密公钥（SM2）
 *       privateKey: "sm2-private-key-for-request"      # 请求解密私钥（SM2）
 *       includePaths:                                   # 需要加解密的路径
 *         - "/api/*"
 *       excludePaths:                                   # 排除加解密的路径
 *         - "/api/public/*"
 * }</pre>
 * <p>
 * <b>配置说明：</b>
 * <ul>
 *     <li>enable：true 时启用接口加解密 Filter</li>
 *     <li>headerFlag：请求头中存放加密 SM4 密钥的字段名</li>
 *     <li>publicKey：用于加密响应中的 SM4 密钥</li>
 *     <li>privateKey：用于解密请求中的 SM4 密钥</li>
 *     <li>includePaths/excludePaths：控制哪些请求需要加解密</li>
 * </ul>
 *
 * @author xiyao
 * @see AlgorithmType
 * @see EncodeType
 */
@Data
@ConfigurationProperties(prefix = "system.crypto.api")
public class EncryptorApi {

    /**
     * 是否开启接口加解密
     * <p>
     * 当设置为 true 时，系统会注册 EncryptorFilter 对请求/响应进行加解密处理。
     *
     * @return true 表示启用，false 表示禁用
     */
    private Boolean enable = false;

    /**
     * 加解密请求头标识
     * <p>
     * 指定请求头中存放加密 SM4 密钥的字段名。
     * 客户端需要将加密后的 SM4 密钥放在此请求头中。
     *
     * @return 请求头标识字段名
     */
    private String headerFlag = "headerFlag";

    /**
     * 响应加密公钥（SM2）
     * <p>
     * 用于加密响应中的 SM4 密钥。客户端使用对应的私钥解密。
     *
     * @return SM2 公钥
     */
    private String publicKey;

    /**
     * 请求解密私钥（SM2）
     * <p>
     * 用于解密请求中的 SM4 密钥。客户端使用对应的公钥加密。
     *
     * @return SM2 私钥
     */
    private String privateKey;

    /**
     * 需要加解密的路径列表
     * <p>
     * 支持 Ant 风格路径匹配，如 /api/* 匹配 /api 开头的所有路径。
     * 默认值空列表表示拦截所有请求（/*）。
     *
     * @return 路径列表
     */
    private java.util.List<String> includePaths = new java.util.ArrayList<>();

    /**
     * 排除加解密的路径列表
     * <p>
     * 优先级高于 includePaths，匹配上的路径不会进行加解密。
     * 适用于不需要加解密的公共接口。
     *
     * @return 排除路径列表
     */
    private java.util.List<String> excludePaths = new java.util.ArrayList<>();

}