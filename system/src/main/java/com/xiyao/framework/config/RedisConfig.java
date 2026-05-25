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
 * <p>
 * 功能：
 * <ol>
 *     <li>配置 RedisTemplate，支持直接操作 Redis</li>
 *     <li>配置 CacheManager，支持 Spring Cache 注解</li>
 *     <li>配置序列化方式（key 用 String，value 用 Jackson JSON）</li>
 * </ol>
 *
 * <p>
 * <b>序列化说明：</b>
 * <ul>
 *     <li>Key：使用 StringRedisSerializer，纯字符串存储</li>
 *     <li>Value：使用 Jackson2JsonRedisSerializer，支持对象序列化</li>
 *     <li>存入 Redis 时会在 JSON 中添加 @class 类型信息，反序列化时可还原具体类型</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 使用 RedisTemplate
 * redisTemplate.opsForValue().set("key", new User());
 * User user = (User) redisTemplate.opsForValue().get("key");
 *
 * // 使用 Spring Cache 注解
 * @Cacheable(value = "user", key = "#id")
 * public User getUserById(Long id) { ... }
 * }</pre>
 *
 * @author xiyao
 */
@Configuration
@EnableCaching  // 启用 Spring Cache 缓存支持
public class RedisConfig implements CachingConfigurer {

    /**
     * 注入 Spring 全局配置的 ObjectMapper
     * <p>
     * 用于 JSON 序列化，来自 JacksonConfig 配置类。
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 配置 RedisTemplate
     * <p>
     * RedisTemplate 是 Spring Data Redis 提供的操作 Redis 的模板类，
     * 支持 String、Hash、List、Set 等数据类型的操作。
     *
     * @param connectionFactory Redis 连接工厂（自动注入）
     * @return RedisTemplate 实例
     */
    @Bean
    @Primary  // 默认注入，避免类型歧义
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);

        // 获取 Jackson JSON 序列化器（用于 value）
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = getJackson2JsonRedisSerializer();

        // 字符串序列化器（用于 key 和 hashKey）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Key 使用 String 序列化（可读性好，便于调试）
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 Jackson 序列化（支持复杂对象）
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        // 初始化模板配置
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 配置缓存管理器（支持多缓存空间不同配置）
     * <p>
     * 用于 @Cacheable、@CacheEvict 等 Spring Cache 注解。
     *
     * @param connectionFactory Redis 连接工厂
     * @return CacheManager 实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 获取 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = getJackson2JsonRedisSerializer();

        // 默认缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // Key 序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Value 序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                // 默认过期时间：1小时
                .entryTtl(Duration.ofHours(1))
                // 不允许缓存 null 值
                .disableCachingNullValues();

        // 构建缓存管理器
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)  // 默认配置
                .transactionAware()    // 事务感知（支持事务内缓存操作）
                .build();
    }

    /**
     * 创建 Jackson2JsonRedisSerializer
     * <p>
     * 配置 Jackson ObjectMapper 的 Redis 专用配置：
     * <ul>
     *     <li>序列化所有字段</li>
     *     <li>在 JSON 中存储 @class 类型信息</li>
     *     <li>支持反序列化时还原具体类型（如 User、List&lt;User&gt;）</li>
     * </ul>
     *
     * @return Jackson2JsonRedisSerializer 实例
     */
    private Jackson2JsonRedisSerializer<Object> getJackson2JsonRedisSerializer() {
        // 复制全局 ObjectMapper，避免修改影响其他配置
        ObjectMapper redisMapper = this.objectMapper.copy();

        // 设置对所有字段进行序列化检测
        redisMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用默认类型：在 JSON 中存储类型信息
        // LaissezFaireSubTypeValidator：宽松的类型验证器，允许所有类型
        // ObjectMapper.DefaultTyping.NON_FINAL：非 final 类才存储类型信息
        // JsonTypeInfo.As.WRAPPER_ARRAY：类型信息以数组形式包装在 JSON 开头
        redisMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);

        return new Jackson2JsonRedisSerializer<>(redisMapper, Object.class);
    }
}