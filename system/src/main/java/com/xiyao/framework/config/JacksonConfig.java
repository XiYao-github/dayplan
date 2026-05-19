package com.xiyao.framework.config;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Jackson JSON 配置类
 */
@Configuration
public class JacksonConfig {

    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    private static final String DEFAULT_TIME_ZONE = "GMT+8";

    /**
     * 配置 Jackson 并注入 ObjectMapper
     * <p>
     * 使用 @Primary 确保此 ObjectMapper 被优先使用
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // 日期时间格式配置
        builder.simpleDateFormat(JacksonConfig.DEFAULT_DATE_TIME_PATTERN);
        builder.timeZone(DEFAULT_TIME_ZONE);

        // 时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // LocalDateTime 序列化/反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_DATE_TIME_PATTERN)));
        // LocalDate 序列化/反序列化
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_DATE_PATTERN)));
        // LocalTime 序列化/反序列化
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(JacksonConfig.DEFAULT_TIME_PATTERN)));

        // Date 序列化/反序列化
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(Date.class, new JsonSerializer<>() {
            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String format = DateUtil.format(value, JacksonConfig.DEFAULT_DATE_TIME_PATTERN);
                gen.writeString(format);
            }
        });
        dateModule.addDeserializer(Date.class, new JsonDeserializer<>() {
            @Override
            public Date deserialize(JsonParser p, DeserializationContext context) throws IOException {
                DateTime parse = DateUtil.parse(p.getText());
                if (ObjectUtils.isNull(parse)) {
                    return null;
                }
                return parse.toJdkDate();
            }
        });

        // Long/BigInteger/BigDecimal 序列化转字符串
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        customModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        customModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // 注册模块
        builder.modules(javaTimeModule, dateModule, customModule);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);

        // 配置属性
        ObjectMapper objectMapper = builder.build();

        // 序列化特性配置
        // 允许序列化空对象
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 日期不转时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 反序列化特性配置
        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 空字符串转为 null
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return objectMapper;
    }
}


