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
 * <p>
 * 配置 Spring MVC 的 JSON 序列化/反序列化行为，
 * 确保前后端交互时日期格式统一、数值类型不丢失精度。
 *
 * <p>
 * <b>核心配置：</b>
 * <ul>
 *     <li>日期时间格式：yyyy-MM-dd HH:mm:ss（北京时间）</li>
 *     <li>Long 类型序列化：转为字符串避免 JavaScript 精度丢失</li>
 *     <li>空值处理：序列化时忽略 null 字段</li>
 *     <li>未知属性：反序列化时忽略未知字段</li>
 * </ul>
 *
 * <p>
 * <b>技术要点：</b>
 * <ul>
 *     <li>使用 @Primary 确保此 ObjectMapper 优先于默认配置</li>
 *     <li>LocalDateTime/LocalDate/LocalTime 使用 JSR310 模块</li>
 *     <li>Long/BigInteger/BigDecimal 转为字符串防止 JS 精度丢失（超过 Number.MAX_SAFE_INTEGER）</li>
 * </ul>
 *
 * @author xiyao
 * @see ObjectMapper
 * @see JavaTimeModule
 */
@Configuration
public class JacksonConfig {

    /** 默认日期时间格式 */
    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 默认日期格式 */
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    /** 默认时间格式 */
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    /** 默认时区（北京时间） */
    private static final String DEFAULT_TIME_ZONE = "GMT+8";

    /**
     * 配置 Jackson ObjectMapper
     * <p>
     * 此方法配置全局的 JSON 序列化/反序列化规则。
     * 配置后的 ObjectMapper 会自动注入到 Spring MVC 的消息转换器中。
     *
     * @return 配置好的 ObjectMapper 实例
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // ========== 日期时间基础配置 ==========
        builder.simpleDateFormat(DEFAULT_DATE_TIME_PATTERN);
        builder.timeZone(DEFAULT_TIME_ZONE);

        // ========== Java 8 时间类型序列化/反序列化 ==========
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // LocalDateTime：格式 yyyy-MM-dd HH:mm:ss
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN)));
        // LocalDate：格式 yyyy-MM-dd
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)));
        // LocalTime：格式 HH:mm:ss
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)));

        // ========== Date 类型序列化/反序列化 ==========
        SimpleModule dateModule = new SimpleModule();
        // Date 序列化为格式化字符串
        dateModule.addSerializer(Date.class, new JsonSerializer<>() {
            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String format = DateUtil.format(value, DEFAULT_DATE_TIME_PATTERN);
                gen.writeString(format);
            }
        });
        // Date 从字符串反序列化
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

        // ========== 数值类型序列化转字符串 ==========
        // 解决 Long 类型在 JavaScript 中精度丢失的问题
        // JavaScript Number.MAX_SAFE_INTEGER = 9007199254740991（16位）
        // Java Long.MAX_VALUE = 9223372036854775807（19位）
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        customModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        customModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // ========== 注册模块并配置序列化包含规则 ==========
        builder.modules(javaTimeModule, dateModule, customModule);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);  // null 字段不序列化

        // ========== 构建 ObjectMapper ==========
        ObjectMapper objectMapper = builder.build();

        // ========== 序列化特性配置 ==========
        // 允许序列化空对象（不抛异常）
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 日期不转时间戳（使用格式化的日期字符串）
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // ========== 反序列化特性配置 ==========
        // 忽略未知属性（不抛异常）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 空字符串转为 null 对象
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return objectMapper;
    }
}


