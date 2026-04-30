package com.xiyao.encrypt.annotation;

import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;

import java.lang.annotation.*;

/**
 * 字段加解密注解
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {

    /**
     * 加密算法
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * 安全秘钥
     */
    String password() default "";

    /**
     * 公钥
     */
    String publicKey() default "";

    /**
     * 私钥
     */
    String privateKey() default "";

    /**
     * 编码方式
     */
    EncodeType encode() default EncodeType.DEFAULT;

}
