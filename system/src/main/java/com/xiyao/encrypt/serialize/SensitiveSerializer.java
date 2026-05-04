package com.xiyao.encrypt.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.xiyao.encrypt.annotation.Sensitive;
import com.xiyao.encrypt.enums.SensitiveStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 数据脱敏序列化器
 *
 */
@Slf4j
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final SensitiveStrategy strategy;

    public SensitiveSerializer() {
        this.strategy = null;
    }

    public SensitiveSerializer(SensitiveStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            // 值为空，写入 null
            gen.writeNull();
        } else if (strategy != null) {
            // 调用策略的 apply 方法进行脱敏
            String desensitized = strategy.apply(value);
            // 写入脱敏字符串
            gen.writeString(desensitized);
        } else {
            // 不使用策略，写入原值
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        // 获取字段注解
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (annotation != null && property.getType().isTypeOrSubTypeOf(String.class)) {
            // 读取注解策略
            SensitiveStrategy strategy = annotation.value();
            // 使用策略创建序列化器
            return new SensitiveSerializer(strategy);
        }
        return prov.findValueSerializer(property.getType(), property);
    }

}
