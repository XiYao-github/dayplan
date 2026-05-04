package com.xiyao.encrypt.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.xiyao.common.constant.Constant;
import com.xiyao.encrypt.annotation.EncryptField;
import com.xiyao.encrypt.core.encryptor.AbstractEncryptor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密管理类
 */
@Slf4j
@NoArgsConstructor
public class EncryptorManager {

    /**
     * 缓存加密器实例
     */
    Map<String, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 类加密字段缓存
     */
    Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 根据配置进行加密
     *
     * @param value          待加密的值
     * @param encryptContext 加解密配置参数
     */
    public String encrypt(String value, EncryptContext encryptContext) {
        // 检查是否携带加密头标识
        if (value.startsWith(Constant.ENCRYPT_HEADER)) {
            // 存在加密头标识，说明是密文，直接返回
            return value;
        }
        // 获取加密器
        IEncryptor encryptor = this.registerEncryptor(encryptContext);
        // 执行加密
        String encrypt = encryptor.encrypt(value);
        // 添加加密头标识
        return Constant.ENCRYPT_HEADER + encrypt;
    }

    /**
     * 根据配置进行解密
     *
     * @param value          待解密的值
     * @param encryptContext 加解密配置参数
     */
    public String decrypt(String value, EncryptContext encryptContext) {
        // 检查是否携带加密头标识
        if (!value.startsWith(Constant.ENCRYPT_HEADER)) {
            // 没有加密头标识，说明是明文，直接返回
            return value;
        }
        // 获取加密器
        IEncryptor encryptor = this.registerEncryptor(encryptContext);
        // 删除加密头标识
        String str = value.substring(Constant.ENCRYPT_HEADER.length());
        // 执行解密
        return encryptor.decrypt(str);
    }

    /**
     * 注册加密执行者到缓存
     *
     * @param encryptContext 加解密配置参数
     */
    public IEncryptor registerEncryptor(EncryptContext encryptContext) {
        // 使用算法名称作为 key
        String key = encryptContext.getAlgorithm().name();
        if (encryptorMap.containsKey(key)) {
            // 命中缓存，直接返回
            return encryptorMap.get(key);
        }
        // 未命中，通过反射创建新实例
        Class<? extends AbstractEncryptor> clazz = encryptContext.getAlgorithm().getClazz();
        IEncryptor encryptor = ReflectUtil.newInstance(clazz, encryptContext);
        encryptorMap.put(key, encryptor);
        return encryptor;
    }

    /**
     * 获取类加密字段缓存
     *
     * @param clazz 加密类
     */
    public Set<Field> getFieldCache(Class<?> clazz) {
        if (fieldCache.containsKey(clazz)) {
            // 命中缓存，直接返回
            Set<Field> fieldSet = fieldCache.get(clazz);
            return Collections.unmodifiableSet(fieldSet);
        }
        // 未命中，通过反射创建新实例
        Set<Field> fieldSet = getEncryptField(clazz);
        if (CollUtil.isNotEmpty(fieldSet)) {
            fieldCache.put(clazz, fieldSet);
        }
        return Collections.unmodifiableSet(fieldSet);
    }

    /**
     * 获得加密类的加密字段集合
     *
     * @param clazz 加密类
     */
    private Set<Field> getEncryptField(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();
        // 接口，匿名类，直接返回
        if (clazz.isInterface() || clazz.isAnonymousClass()) {
            return fieldSet;
        }
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                // 存在加密注解，字段类型必须是 String
                if (field.isAnnotationPresent(EncryptField.class) && field.getType() == String.class) {
                    field.setAccessible(true);
                    fieldSet.add(field);
                }
            }
            // 加载父类字段
            current = current.getSuperclass();
        }
        return fieldSet;
    }

}
