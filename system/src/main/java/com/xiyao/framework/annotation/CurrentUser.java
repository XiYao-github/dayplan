package com.xiyao.framework.annotation;


import java.lang.annotation.*;

/**
 * 当前登录用户注解
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}