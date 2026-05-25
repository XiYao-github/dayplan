package com.xiyao.crypto.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.xiyao.crypto.annotation.CryptoField;
import com.xiyao.crypto.core.encryptor.AbstractEncryptor;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密管理器
 * <p>
 * 负责管理加密器实例缓存和类加密字段缓存，
 * 提供统一的加密/解密入口，支持 SM2/SM3/SM4 等多种加密算法。
 *
 * <p>
 * <b>加密标识：</b>
 * 所有加密后的值都会添加 "ENC_" 前缀标识，
 * 解密时通过前缀判断是否为密文，避免重复解密。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 加密
 * String ciphertext = encryptorManager.encrypt("敏感数据", context);
 * // 解密
 * String plaintext = encryptorManager.decrypt(ciphertext, context);
 * }</pre>
 *
 * @author xiyao
 */
@NoArgsConstructor
public class EncryptorManager {

    /**
     * 加密标识前缀
     * <p>
     * 添加在加密后的字符串开头，用于标识该值已经是密文。
     * 解密时会检查此前缀，有则解密，无则直接返回原值。
     */
    public static final String ENCRYPT_HEADER = "ENC_";

    /**
     * 加密器实例缓存
     * <p>
     * Key：算法名称（如 "SM4"），Value：加密器实例。
     * 使用 ConcurrentHashMap 保证线程安全。
     */
    Map<String, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 类加密字段缓存
     * <p>
     * 缓存带有 @CryptoField 注解的字段，
     * 避免每次解密都遍历反射查找字段。
     */
    Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 根据配置执行加密
     * <p>
     * 加密流程：
     * <ol>
     *     <li>检查是否已加密（有 ENC_ 前缀直接返回）</li>
     *     <li>获取/创建对应算法的加密器</li>
     *     <li>执行加密并添加 ENC_ 前缀</li>
     * </ol>
     *
     * @param value   待加密的明文
     * @param context 加解密配置参数（算法、密钥等）
     * @return 加密后的密文（带 ENC_ 前缀）
     */
    public String encrypt(String value, EncryptContext context) {
        // 检查是否已经加密（有前缀说明是密文，跳过）
        if (value.startsWith(ENCRYPT_HEADER)) {
            return value;
        }

        // 获取对应算法的加密器实例
        IEncryptor encryptor = this.registerEncryptor(context);

        // 执行加密操作
        String encrypt = encryptor.encrypt(value, context.getEncode());

        // 添加加密标识前缀，返回带 ENC_ 的密文
        return ENCRYPT_HEADER + encrypt;
    }

    /**
     * 根据配置执行解密
     * <p>
     * 解密流程：
     * <ol>
     *     <li>检查是否携带 ENC_ 前缀，无则直接返回原值</li>
     *     <li>获取/创建对应算法的加密器</li>
     *     <li>去除前缀后执行解密</li>
     * </ol>
     *
     * @param value   待解密的密文（可能带 ENC_ 前缀）
     * @param context 加解密配置参数
     * @return 解密后的明文
     */
    public String decrypt(String value, EncryptContext context) {
        // 没有加密前缀，说明是明文，无需解密
        if (!value.startsWith(ENCRYPT_HEADER)) {
            return value;
        }

        // 获取对应算法的加密器实例
        IEncryptor encryptor = this.registerEncryptor(context);

        // 去除加密前缀
        String str = value.substring(ENCRYPT_HEADER.length());

        // 执行解密操作
        return encryptor.decrypt(str);
    }

    /**
     * 注册/获取加密器
     * <p>
     * 优先从缓存获取，已存在则直接返回。
     * 不存在则根据配置通过反射创建新的加密器实例并缓存。
     *
     * @param context 加解密配置参数
     * @return 对应算法的加密器实例
     */
    public IEncryptor registerEncryptor(EncryptContext context) {
        String key = context.getAlgorithm().name();

        // 命中缓存，直接返回
        if (encryptorMap.containsKey(key)) {
            return encryptorMap.get(key);
        }

        // 未命中，通过反射创建加密器实例
        // 根据算法类型获取加密器 Class
        Class<? extends AbstractEncryptor> clazz = context.getAlgorithm().getClazz();

        // 通过反射实例化（传入配置参数）
        IEncryptor encryptor = ReflectUtil.newInstance(clazz, context);

        // 存入缓存
        encryptorMap.put(key, encryptor);

        return encryptor;
    }

    /**
     * 获取类的加密字段缓存
     * <p>
     * 缓存带有 @CryptoField 注解且类型为 String 的字段。
     *
     * @param clazz 待检查的类
     * @return 加密字段集合
     */
    public Set<Field> getFieldCache(Class<?> clazz) {
        // 命中缓存，直接返回
        if (fieldCache.containsKey(clazz)) {
            Set<Field> fieldSet = fieldCache.get(clazz);
            return Collections.unmodifiableSet(fieldSet);
        }

        // 未命中，扫描并缓存
        Set<Field> fieldSet = getEncryptField(clazz);
        if (CollUtil.isNotEmpty(fieldSet)) {
            fieldCache.put(clazz, fieldSet);
        }
        return Collections.unmodifiableSet(fieldSet);
    }

    /**
     * 扫描获取类中所有带 @CryptoField 注解的字段
     * <p>
     * 遍历类及其父类的所有声明字段，
     * 筛选出标注了 @CryptoField 且类型为 String 的字段。
     *
     * @param clazz 待扫描的类
     * @return 加密字段集合
     */
    private Set<Field> getEncryptField(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();

        // 接口、内部类、匿名类不处理
        if (clazz.isInterface() || clazz.isMemberClass() || clazz.isAnonymousClass()) {
            return fieldSet;
        }

        // 遍历当前类及其父类
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                // 检查是否有 @CryptoField 注解，且字段类型必须是 String
                if (field.isAnnotationPresent(CryptoField.class) && field.getType() == String.class) {
                    // 设置可访问性（私有字段也能操作）
                    field.setAccessible(true);
                    fieldSet.add(field);
                }
            }
            // 继续遍历父类
            current = current.getSuperclass();
        }

        return fieldSet;
    }
}