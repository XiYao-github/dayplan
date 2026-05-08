package com.xiyao.encrypt.annotation;

import java.lang.annotation.*;

/**
 * 字段加解密注解
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {
}
