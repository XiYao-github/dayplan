package com.xiyao.dict.converter;

import com.xiyao.dict.config.DictCache;
import com.xiyao.dict.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 字典枚举转换器工厂
 * <p>
 * 实现 Spring ConverterFactory 接口，
 * 将请求参数（String）自动转换为实现了 BaseEnum 接口的枚举类型。
 *
 * <p>
 * <b>支持三种匹配方式（按优先级）：</b>
 * <ol>
 *     <li>按枚举名字匹配（如 "NORMAL"）</li>
 *     <li>按描述匹配（如 "正常"）</li>
 *     <li>按 code 值匹配（如 "1"）</li>
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
 *     // 请求 /api/user?status=1 或 /api/user?status=NORMAL 或 /api/user?status=正常 都能匹配
 *     return Result.ok(userService.listByStatus(status));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see BaseEnum
 * @see DictCache
 */
@Component
public class DictEnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    /**
     * 获取指定枚举类型的转换器
     *
     * @param targetType 目标枚举类型
     * @param <T>        枚举类型
     * @return String 到目标枚举的转换器
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    /**
     * String 到枚举的转换器
     * <p>
     * 内部类，实现具体的转换逻辑。
     *
     * @param <T> 枚举类型
     * @author xiyao
     */
    private static class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        /** 目标枚举类型 */
        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        /**
         * 将字符串转换为枚举
         * <p>
         * 按优先级尝试匹配：名字 -> 描述 -> code值
         *
         * @param source 输入字符串
         * @return 对应的枚举常量，找不到返回 null
         */
        @Override
        public T convert(@NonNull String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            String input = source.trim();

            // 1. 先尝试按枚举名字匹配（最高优先级）
            T enumConstant = DictCache.getInstance().getEnumByName(enumType, input);
            if (enumConstant != null) {
                return enumConstant;
            }

            // 2. 按描述匹配（次优先级）
            enumConstant = DictCache.getInstance().getEnumByDesc(enumType, input);
            if (enumConstant != null) {
                return enumConstant;
            }

            // 3. 按 code 值匹配（最后优先级）
            enumConstant = DictCache.getInstance().getEnumByCode(enumType, input);
            if (enumConstant != null) {
                return enumConstant;
            }

            // 都匹配不上，返回 null
            return null;
        }
    }
}