package com.xiyao.dict.converter;

import com.xiyao.dict.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 枚举转换器工厂
 * <p>
 * 实现 Spring ConverterFactory 接口，
 * 将请求参数（String）自动转换为实现了 BaseEnum 接口的枚举类型。
 *
 * <p>
 * <b>支持三种匹配方式（按优先级）：</b>
 * <ol>
 *     <li>按枚举的 code 值匹配（如 "1"）</li>
 *     <li>按枚举的 desc 描述匹配（如 "正常"）</li>
 *     <li>按枚举的名字匹配（如 "NORMAL"）</li>
 * </ol>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>Controller 参数绑定（@RequestParam）</li>
 *     <li>PathVariable 参数绑定</li>
 *     <li>请求体字段绑定</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // Controller 中直接使用枚举类型作为参数
 * @GetMapping("/user")
 * public Result&lt;List&lt;User&gt;&gt; getUsers(@RequestParam DataStatus status) {
 *     // 请求 /api/user?status=1 或 /api/user?status=正常 都能匹配
 *     return Result.ok(userService.listByStatus(status));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see BaseEnum
 * @see DictEnumConverterFactory
 */
@Component
public class MyEnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    /**
     * 获取指定枚举类型的转换器
     *
     * @param targetType 目标枚举类型
     * @param <T>        枚举类型
     * @return String 到目标枚举的转换器
     */
    @Override
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    /**
     * String 到枚举的转换器
     * <p>
     * 内部类，实现具体的转换逻辑。
     * 按优先级尝试匹配：code -> desc -> name
     *
     * @param <T> 枚举类型
     */
    private static final class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        /** 目标枚举类型 */
        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        /**
         * 将字符串转换为枚举
         * <p>
         * 按优先级尝试匹配：code -> desc -> name
         * 匹配成功返回对应的枚举常量，都匹配不上返回 null。
         *
         * @param source 输入字符串
         * @return 对应的枚举常量，找不到返回 null
         */
        @Override
        public T convert(@NonNull String source) {
            // 空值判断
            if (!StringUtils.hasText(source)) {
                return null;
            }
            String input = source.trim();

            // 遍历枚举常量，按优先级匹配
            for (T enumConstant : enumType.getEnumConstants()) {
                // 1. 按 code 值匹配（最高优先级）
                if (enumConstant.getCode().toString().equals(input)) {
                    return enumConstant;
                }
                // 2. 按描述匹配
                if (enumConstant.getDesc().equals(input)) {
                    return enumConstant;
                }
                // 3. 按枚举名匹配（最低优先级）
                if (((Enum<?>) enumConstant).name().equals(input)) {
                    return enumConstant;
                }
            }
            // 都匹配不上，返回 null
            return null;
        }
    }
}