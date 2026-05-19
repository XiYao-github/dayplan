package com.xiyao.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类
 */
@Configuration
@EnableCaching  // 启用缓存
public class RedisConfig implements CachingConfigurer {

    /**
     * 注入 Spring 全局配置的 ObjectMapper（来自 JacksonConfig）
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 配置 RedisTemplate，用于直接操作 Redis
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        // 获取 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = getJackson2JsonRedisSerializer();
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
     * 缓存管理器配置（支持多缓存空间不同配置）
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 获取 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = getJackson2JsonRedisSerializer();
        // 默认缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置key序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                // 默认过期时间1小时
                .entryTtl(Duration.ofHours(1))
                // 不允许缓存null值
                .disableCachingNullValues();

        // 构建缓存管理器
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)  // 默认配置
                .transactionAware()  // 事务感知
                .build();
    }

    /**
     * 创建 Jackson2JsonRedisSerializer，配置 redisMapper
     */
    private Jackson2JsonRedisSerializer<Object> getJackson2JsonRedisSerializer() {
        ObjectMapper redisMapper = this.objectMapper.copy();
        // 设置对所有字段进行序列化检测
        redisMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型：在 JSON 中存储 @class 信息，以便反序列化时还原具体类型
        redisMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,   // 允许的类型验证器（宽松）
                ObjectMapper.DefaultTyping.NON_FINAL,    // 非 final 类才存储类型信息
                JsonTypeInfo.As.WRAPPER_ARRAY);          // 类型信息以数组形式包装
        return new Jackson2JsonRedisSerializer<>(redisMapper, Object.class);
    }

}
