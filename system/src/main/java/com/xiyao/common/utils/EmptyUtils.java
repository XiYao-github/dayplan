package com.xiyao.common.utils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 判空工具类
 */
public class EmptyUtils {

    // ==================== 对象判空 ====================

    /**
     * 判断对象是否为空
     * 支持：null、字符串、数组、集合、Map
     *
     * @param obj 待判断对象
     * @return true:为空 false:不为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        // Optional
        if (obj instanceof Optional) {
            return ((Optional<?>) obj).isEmpty();
        }

        // 字符串
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).isEmpty();
        }

        // 数组
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }

        // 集合
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }

        // Map
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    // ==================== 字符串特判 ====================

    /**
     * 判断对象是否为空白、null、空串、纯空格、制表符等
     *
     * @param str 待判断字符串
     * @return true:为空 false:不为空
     */
    public static boolean isBlank(CharSequence str) {
        if (isEmpty(str)) {
            return true;
        }
        return str.toString().isBlank();
    }

    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    // ==================== 集合特判 ====================

    /**
     * 空集合包装：null转为空集合 List
     */
    public static <T> List<T> nullToEmpty(List<T> list) {
        return isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 空集合包装：null转为空集合 Set
     */
    public static <T> Set<T> nullToEmpty(Set<T> set) {
        return isEmpty(set) ? Collections.emptySet() : set;
    }

    /**
     * 空集合包装：null转为空集合 Map
     */
    public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return isEmpty(map) ? Collections.emptyMap() : map;
    }


}