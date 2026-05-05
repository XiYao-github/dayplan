package com.xiyao.mybatisplus.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate，用于直接操作 Redis
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        // 获取 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = jackson2JsonRedisSerializer();
        // 字符串序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // key 和 hash key 使用 String 序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value 和 hash value 使用 Jackson 序列化
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);
        // 初始化，检查配置
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 RedisCacheManager，用于 @Cacheable 等注解的缓存管理
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 获取 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = jackson2JsonRedisSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // key 使用字符串序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // value 使用 JSON 序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                // 默认缓存过期时间为 1 小时
                .entryTtl(Duration.ofHours(1))
                // 禁止缓存 null 值
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }

    /**
     * 创建 Jackson2JsonRedisSerializer，配置 ObjectMapper
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置对所有字段进行序列化检测
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型：在 JSON 中存储 @class 信息，以便反序列化时还原具体类型
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,   // 允许的类型验证器（宽松）
                ObjectMapper.DefaultTyping.NON_FINAL,    // 非 final 类才存储类型信息
                JsonTypeInfo.As.WRAPPER_ARRAY);          // 类型信息以数组形式包装
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}