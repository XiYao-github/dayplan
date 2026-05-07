package com.xiyao.framework.config;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.xiyao.common.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 序列化配置类
 */
@Slf4j
@Configuration
public class JacksonConfig {

    /**
     * 配置 Jackson 并注入 ObjectMapper（覆盖 Spring Boot 默认配置）
     * 使用 @Primary 确保此 ObjectMapper 被优先使用
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // ==================== 基础格式配置 ====================
        // 全局日期时间格式：yyyy-MM-dd HH:mm:ss
        builder.simpleDateFormat(Constant.PATTERN_DATE_TIME);
        // 全局时区：GMT+8
        builder.timeZone(Constant.TIME_ZONE);
        // 序列化时排除 null 值，精简响应体，减少网络传输量
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);

        // ==================== 序列化配置（Java → JSON） ====================
        // 日期不转时间戳：禁用默认的数组格式 [2024,1,15,14,30,0]，输出字符串（如 "2024-01-15 14:30:00"），避免前端解析数组格式的麻烦
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 启用枚举的 toString() 值输出，便于前端直接展示中文（枚举需重写 toString 方法）
        builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        // ==================== 反序列化配置（JSON → Java） ====================
        // 忽略未知属性，前端多传字段时不报错，只反序列化已知字段，提升接口兼容性
        builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // ==================== Java 8 时间类型支持 ====================
        // 注册 JavaTimeModule 并自定义 LocalDateTime 格式 LocalDate、LocalTime 等其他时间类型使用 JavaTimeModule 默认配置即可
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // LocalDateTime 序列化格式：yyyy-MM-dd HH:mm:ss
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constant.PATTERN_DATE_TIME)));
        // LocalDateTime 反序列化格式：yyyy-MM-dd HH:mm:ss
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(Constant.PATTERN_DATE_TIME)));

        // 注册 SimpleModule 将 Long 类型序列化为 String 字符串
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 注册模块
        builder.modules(javaTimeModule, longModule);

        return builder.build();
    }
}