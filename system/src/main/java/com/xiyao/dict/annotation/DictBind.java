package com.xiyao.dict.annotation;

import java.lang.annotation.*;

/**
 * 字段数据绑定注解
 * <p>
 * 标注在实体字段上，指定该字段为字典值字段，
 * MyBatis 拦截器会自动查询字典表，将对应的描述文本填充到 target 字段
 */
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictBind {

    /**
     * 字典类型编码
     * <p>
     * 用于查询 dict_data 表的 dict_code 字段
     *
     * @return 字典编码
     */
    String code();

    /**
     * 字典描述回显目标字段
     * <p>
     * 不指定时默认使用原字段名 + "Text"
     * 例如：status 字段对应填充 statusText 字段
     *
     * @return 目标字段名
     */
    String target() default "";
}