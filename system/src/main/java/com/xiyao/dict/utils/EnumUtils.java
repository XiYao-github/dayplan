package com.xiyao.dict.utils;

import com.xiyao.dict.enums.BaseEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 枚举通用工具类
 */
@Slf4j
public class EnumUtils {

    /**
     * 根据 value 匹配枚举（支持 code、desc、name）
     *
     * @param enumClass 枚举类
     * @param value     传入的值
     * @return 枚举实例
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