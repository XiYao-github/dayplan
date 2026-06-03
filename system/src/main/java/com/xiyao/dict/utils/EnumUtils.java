package com.xiyao.dict.utils;

import com.xiyao.dict.enums.BaseEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 枚举通用工具类
 * <p>
 * 提供枚举相关的通用操作方法，封装枚举值转换的通用逻辑。
 * 该类是 BaseEnum.fromValue() 的便捷调用包装。
 *
 * <p>
 * <b>注意：</b>
 * 实际转换逻辑委托给 {@link BaseEnum#fromValue(Class, Object)}，
 * 此类主要提供便捷的静态方法调用方式。
 *
 * @author xiyao
 * @see BaseEnum
 */
@Slf4j
public class EnumUtils {

    /**
     * 根据值匹配枚举
     * <p>
     * 支持三种匹配方式：code（存储值）、desc（描述文本）、name（枚举名称）。
     * 匹配规则参见 {@link BaseEnum#fromValue(Class, Object)}。
     *
     * @param enumClass 枚举类 Class
     * @param value     待匹配的值，支持 Integer、String 等类型
     * @param <T>       枚举类型
     * @return 匹配的枚举实例，未找到返回 null
     */
    public static <T extends BaseEnum<?>> T fromValue(Class<T> enumClass, Object value) {
        if (value == null) {
            return null;
        }

        String strValue = String.valueOf(value);

        for (T constant : enumClass.getEnumConstants()) {
            // 匹配 code
            if (String.valueOf(constant.getCode()).equals(strValue)) {
                return constant;
            }
            // 匹配 desc
            if (constant.getDesc().equals(strValue)) {
                return constant;
            }
            // 匹配 name（枚举名称）
            if (((Enum<?>) constant).name().equals(strValue)) {
                return constant;
            }
        }
        return null;
    }
}