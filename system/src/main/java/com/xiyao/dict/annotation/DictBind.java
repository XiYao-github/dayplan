package com.xiyao.dict.annotation;

import java.lang.annotation.*;

/**
 * 字段数据绑定
 */
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictBind {

    /**
     * 字典类型编码
     */
    String code();

    /**
     * 字典描述回显文本字段
     * 不指定时默认使用原字段名 + "Text"
     */
    String target() default "";
}
