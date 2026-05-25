package com.xiyao.encrypt.annotation;

import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;

import java.lang.annotation.*;

/**
 * 字段加解密注解
 * <p>
 * 用于标记实体类中的字段是否为加解密字段。在数据入库前自动加密，出库后自动解密。
 * 支持 SM2（非对称加密）和 SM4（对称加密）两种算法，以及 BASE64 和 HEX 两种编码格式。
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     // 使用默认配置（读取 yml 中的全局配置）
 *     @EncryptField
 *     private String idCard;
 *
 *     // 指定算法和编码
 *     @EncryptField(algorithm = AlgorithmType.SM4, encode = EncodeType.HEX, password = "your-key")
 *     private String bankCard;
 *
 *     // 使用 SM2 非对称加密
 *     @EncryptField(algorithm = AlgorithmType.SM2, publicKey = "sm2-public-key", privateKey = "sm2-private-key")
 *     private String phone;
 * }
 * }</pre>
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>入参时：EncryptInterceptor 拦截请求，对带有 @EncryptField 注解的字段进行加密</li>
 *     <li>出参时：DecryptInterceptor 拦截响应，对带有 @EncryptField 注解的字段进行解密</li>
 *     <li>加密后的数据会存储到数据库，查询时自动解密返回</li>
 * </ol>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>注解只能用于 String 类型字段</li>
 *     <li>字段必须有 getter 和 setter 方法</li>
 *     <li>建议配合 MyBatis-Plus 使用</li>
 * </ul>
 *
 * @author xiyao
 * @see AlgorithmType
 * @see EncodeType
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {

    /**
     * 加密算法
     * <p>
     * 指定该字段使用的加密算法。默认为 DEFAULT，表示使用 yml 配置文件中的全局算法配置。
     *
     * @return 加密算法类型
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * 编码方式
     * <p>
     * 指定加密后的编码格式。默认为 DEFAULT，表示使用 yml 配置文件中的全局编码配置。
     *
     * @return 编码类型
     */
    EncodeType encode() default EncodeType.DEFAULT;

    /**
     * 密钥（对称加密算法）
     * <p>
     * 仅用于 SM4 等对称加密算法。如果为空，则使用 yml 配置文件中的全局密钥。
     *
     * @return SM4 密钥
     */
    String password() default "";

    /**
     * 公钥（非对称加密算法）
     * <p>
     * 仅用于 SM2 等非对称加密算法，用于加密数据。如果为空，则使用 yml 配置文件中的全局公钥。
     *
     * @return SM2 公钥
     */
    String publicKey() default "";

    /**
     * 私钥（非对称加密算法）
     * <p>
     * 仅用于 SM2 等非对称加密算法，用于解密数据。如果为空，则使用 yml 配置文件中的全局私钥。
     *
     * @return SM2 私钥
     */
    String privateKey() default "";

}