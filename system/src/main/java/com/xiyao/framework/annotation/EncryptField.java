package com.xiyao.framework.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)      // 只能用在字段上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留
public @interface EncryptField {
}
