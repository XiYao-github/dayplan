package com.xiyao.mybatisplus.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存，无过期时间
     */
    public void set(final String key, final Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存，有过期时间（秒）
     */
    public void set(final String key, final Object value, final long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存，转换指定类型
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 缓存更新过期时间
     */
    public boolean expire(final String key, final long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

}