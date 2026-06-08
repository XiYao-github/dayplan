package com.xiyao.framework.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具类
 * <p>
 * 封装常用的 Redis 操作，提供更简洁的 API。
 * 支持 String、Hash、List、Set 等数据类型的操作。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 存储字符串
 * redisUtils.set("name", "张三");
 * redisUtils.set("name", "张三", 3600);  // 1小时后过期
 *
 * // 获取数据
 * String name = (String) redisUtils.get("name");
 * User user = redisUtils.get("user:1", User.class);
 *
 * // 删除
 * redisUtils.delete("name");
 *
 * // 设置过期
 * redisUtils.expire("name", 3600);
 * }</pre>
 *
 * @author xiyao
 */
@Component
public class RedisUtils {

    /**
     * Redis 模板
     * <p>
     * Spring Data Redis 提供的模板类，用于执行 Redis 操作。
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== String 操作 ====================

    /**
     * 设置缓存（无过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(final String key, final Object value) {
        if (StrUtil.isBlank(key)) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（指定过期时间，单位：秒）
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param timeout  过期时间（秒）
     */
    public void set(final String key, final Object value, final long timeout) {
        if (StrUtil.isBlank(key)) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，找不到返回 null
     */
    public Object get(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存并转换为指定类型
     *
     * @param key   缓存键
     * @param clazz 目标类型 Class
     * @param <T>   目标类型
     * @return 转换后的缓存值，找不到返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        if (StrUtil.isBlank(key) || ObjectUtil.isNull(clazz)) {
            return null;
        }
        Object value = get(key);
        if (ObjectUtil.isNull(value)) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    // ==================== Key 操作 ====================

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return true 删除成功，false 删除失败
     */
    public boolean delete(final String key) {
        if (StrUtil.isBlank(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    /**
     * 更新缓存过期时间
     *
     * @param key 缓存键
     * @param time 过期时间（秒）
     * @return true 设置成功，false 设置失败
     */
    public boolean expire(final String key, final long time) {
        if (StrUtil.isBlank(key)) {
            return false;
        }
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }
}