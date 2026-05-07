package com.xiyao.framework.utils;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring Bean 工具类
 * <p>
 * 用于在非 Spring 管理的类中获取 Spring 容器中的 Bean。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * UserService userService = SpringContextUtils.getBean(UserService.class);
 * </pre>
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextUtils.applicationContext = context;
    }

    /**
     * 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 根据类型获取 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   泛型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据名称获取 Bean
     *
     * @param name Bean 名称
     * @param <T>  泛型
     * @return Bean 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        if (applicationContext == null) {
            return null;
        }
        return (T) applicationContext.getBean(name);
    }

    /**
     * 根据名称和类型获取 Bean
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   泛型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取当前环境
     *
     * @return 环境名称（dev/test/prod）
     */
    public static String getActiveProfile() {
        if (applicationContext == null) {
            return null;
        }
        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : null;
    }
}