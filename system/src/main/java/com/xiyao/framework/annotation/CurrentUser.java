package com.xiyao.framework.annotation;


import java.lang.annotation.*;

/**
 * 后台登录用户注解
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}