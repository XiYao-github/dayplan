package com.xiyao.encrypt.annotation;

import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;

import java.lang.annotation.*;

/**
 * 字段加解密注解
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {

    /**
     * 加密算法
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * 编码方式
     */
    EncodeType encode() default EncodeType.DEFAULT;

    /**
     * 密钥(对称加密算法)
     */
    String password() default "";

    /**
     * 公钥(非对称加密算法)
     */
    String publicKey() default "";

    /**
     * 私钥(非对称加密算法)
     */
    String privateKey() default "";

}
