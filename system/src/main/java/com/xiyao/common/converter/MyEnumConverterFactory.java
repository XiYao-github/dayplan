package com.xiyao.common.converter;


import com.xiyao.common.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MyEnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    @Override
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static final class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            // 空值判断
            if (!StringUtils.hasText(source)) {
                return null;
            }
            String input = source.trim();
            for (T enumConstant : enumType.getEnumConstants()) {
                // 匹配 code
                if (enumConstant.getCode().toString().equals(input)) {
                    return enumConstant;
                }
                // 匹配 desc
                if (enumConstant.getDesc().equals(input)) {
                    return enumConstant;
                }
                // 匹配枚举名
                if (((Enum<?>) enumConstant).name().equals(input)) {
                    return enumConstant;
                }
            }
            // 都匹配不上，返回 null
            return null;
        }
    }
}