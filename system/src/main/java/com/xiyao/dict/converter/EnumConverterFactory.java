package com.xiyao.dict.converter;

import com.xiyao.dict.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

public class EnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    @Override
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static final class StringToEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        private final Class<T> enumClass;

        public StringToEnumConverter(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public T convert(String source) {
            return BaseEnum.fromValue(enumClass, source);
        }
    }
}
