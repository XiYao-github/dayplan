package com.xiyao.dict.converter;

import com.xiyao.dict.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * Spring 枚举转换器工厂
 * <p>
 * 实现 Spring 的 ConverterFactory 接口，将 String 类型请求参数转换为 BaseEnum 子类。
 * 用于 Controller 层接收枚举类型的请求参数时自动转换。
 *
 * <p>
 * <b>工作原理：</b>
 * <ul>
 *     <li>当 Spring MVC 接收到 String 类型的参数但需要转换为枚举时调用</li>
 *     <li>根据目标枚举类型获取对应的 Converter</li>
 *     <li>Converter 调用 BaseEnum.fromValue() 完成转换</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // Controller 方法中使用枚举参数
 * @GetMapping("/users")
 * public Result<?> queryUsers(Status status) {
 *     // Spring 会自动将请求参数 "1" 转换为 Status.NORMAL
 * }
 * }</pre>
 *
 * @param <S> 源类型，当前固定为 String
 * @param <T> 目标枚举类型，必须实现 BaseEnum
 * @author xiyao
 * @see BaseEnum
 * @see com.xiyao.dict.utils.EnumUtils
 */
public class EnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    /**
     * 获取指定枚举类型的转换器
     *
     * @param targetType 目标枚举类型
     * @param <T>        枚举类型
     * @return String 到 T 的转换器
     */
    @Override
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        // 返回针对目标枚举类型的转换器实例
        return new StringToEnumConverter<>(targetType);
    }

    /**
     * String 到枚举的转换器内部类
     * <p>
     * 负责将 String 类型的值转换为对应的枚举实例。
     *
     * @param <T> 枚举类型，必须实现 BaseEnum
     */
    private static final class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        /**
         * 目标枚举类的 Class 对象
         */
        private final Class<T> enumClass;

        /**
         * 构造函数
         *
         * @param enumClass 目标枚举类
         */
        public StringToEnumConverter(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        /**
         * 执行转换
         * <p>
         * 将 String 值转换为对应的枚举实例。
         *
         * @param source String 类型的值（如 "1"、"DISABLED"、"正常"）
         * @return 对应的枚举实例，未找到返回 null
         */
        @Override
        public T convert(String source) {
            // 委托给 BaseEnum.fromValue 完成转换逻辑
            return BaseEnum.fromValue(enumClass, source);
        }
    }
}
