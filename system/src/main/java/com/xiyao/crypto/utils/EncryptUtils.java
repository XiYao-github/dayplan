package com.xiyao.crypto.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

/**
 * 国密加密工具类
 * <p>
 * 提供 SM2/SM4 国密算法的便捷加密解密方法。
 * 基于 Hutool 底层加密库封装，提供统一的工具类入口。
 *
 * <p>
 * <b>算法说明：</b>
 * <ul>
 *     <li>SM4：对称加密算法，密钥长度 16 位（128位），加密解密使用同一密钥</li>
 *     <li>SM2：非对称加密算法，公钥加密、私钥解密，常用于密钥交换</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // SM4 加密
 * String ciphertext = EncryptUtils.encryptBySm4Hex(plaintext, password);
 * // SM4 解密
 * String plaintext = EncryptUtils.decryptBySm4(ciphertext, password);
 *
 * // SM2 加密（公钥加密）
 * String ciphertext = EncryptUtils.encryptBySm2Hex(plaintext, publicKey);
 * // SM2 解密（私钥解密）
 * String plaintext = EncryptUtils.decryptBySm2(ciphertext, privateKey);
 * }</pre>
 *
 * @author xiyao
 * @see SM4
 * @see SM2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncryptUtils {

    /**
     * SM4 加密（Base64 编码）
     * <p>
     * 使用 SM4 对称加密算法加密字符串。
     * SM4 密钥长度必须为 16 字符。
     *
     * @param data     待加密的明文字符串
     * @param password SM4 密钥（16 位字符串）
     * @return Base64 编码的密文字符串
     * @throws IllegalArgumentException 密钥为空或长度不为 16 位
     */
    public static String encryptBySm4(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        // SM4 算法的密钥要求是 16 位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * SM4 加密（Hex 编码）
     * <p>
     * 使用 SM4 对称加密算法加密字符串，输出 Hex 编码。
     *
     * @param data     待加密的明文字符串
     * @param password SM4 密钥（16 位字符串）
     * @return Hex 编码的密文字符串
     * @throws IllegalArgumentException 密钥为空或长度不为 16 位
     */
    public static String encryptBySm4Hex(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * SM4 解密
     * <p>
     * 使用 SM4 对称加密算法解密字符串。
     * 支持 Base64 或 Hex 编码的密文输入。
     *
     * @param data     待解密的密文字符串（Base64 或 Hex 编码）
     * @param password SM4 密钥（16 位字符串）
     * @return 解密后的明文字符串
     * @throws IllegalArgumentException 密钥为空或长度不为 16 位
     */
    public static String decryptBySm4(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.decryptStr(data, StandardCharsets.UTF_8);
    }

    /**
     * SM2 公钥加密（Base64 编码）
     * <p>
     * 使用 SM2 非对称加密算法和公钥加密字符串。
     *
     * @param data      待加密的明文字符串
     * @param publicKey SM2 公钥（Hex 编码）
     * @return Base64 编码的密文字符串
     * @throws IllegalArgumentException 公钥为空
     */
    public static String encryptBySm2(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new IllegalArgumentException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptBase64(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * SM2 公钥加密（Hex 编码）
     * <p>
     * 使用 SM2 非对称加密算法和公钥加密字符串，输出 Hex 编码。
     *
     * @param data      待加密的明文字符串
     * @param publicKey SM2 公钥（Hex 编码）
     * @return Hex 编码的密文字符串
     * @throws IllegalArgumentException 公钥为空
     */
    public static String encryptBySm2Hex(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new IllegalArgumentException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptHex(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * SM2 私钥解密
     * <p>
     * 使用 SM2 非对称加密算法和私钥解密字符串。
     *
     * @param data       待解密的密文字符串
     * @param privateKey SM2 私钥（Hex 编码）
     * @return 解密后的明文字符串
     * @throws IllegalArgumentException 私钥为空
     */
    public static String decryptBySm2(String data, String privateKey) {
        if (StrUtil.isBlank(privateKey)) {
            throw new IllegalArgumentException("SM2需要传入私钥进行解密");
        }
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.decryptStr(data, KeyType.PrivateKey, StandardCharsets.UTF_8);
    }

    /**
     * 主方法 - 生成密钥对和加解密演示
     * <p>
     * 演示如何生成 SM2 密钥对、SM4 密钥，以及完整的加解密流程。
     */
    public static void main(String[] args) {
        System.out.println("========== 生成 SM2 密钥对 ==========");

        // 生成 SM2 密钥对
        KeyPair keyPair = KeyUtil.generateKeyPair("SM2");

        String privateKey = HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(keyPair.getPrivate()));
        String publicKey = HexUtil.encodeHexStr(BCUtil.encodeECPublicKey(keyPair.getPublic()));

        System.out.println("SM2 私钥(Hex): " + privateKey);
        System.out.println("SM2 公钥(Hex): " + publicKey);

        System.out.println("========== 生成 SM4 密钥 ==========");

        String password = RandomUtil.randomStringUpper(16);
        System.out.println("SM4 密钥(字符串): " + password);
        System.out.println("密钥字节长度: " + password.length());

        System.out.println("========== 原始数据 ==========");
        String plainText = "这是需要加密的敏感数据，内容可能很长...";
        System.out.println("明文: " + plainText);

        System.out.println("========== 加密过程 ==========");

        // 使用 SM4 加密明文
        String encryptDataHex = encryptBySm4Hex(plainText, password);
        System.out.println("SM4 加密后的数据(Hex): " + encryptDataHex);

        // 使用 SM2 公钥加密 SM4 密钥
        String encryptSm4KeyHex = encryptBySm2Hex(password, publicKey);
        System.out.println("SM2 加密后的SM4密钥(Hex): " + encryptSm4KeyHex);

        System.out.println("========== 5. 解密过程 ==========");

        // 使用 SM2 私钥解密，获取 SM4 密钥
        String decryptSm4Key = decryptBySm2(encryptSm4KeyHex, privateKey);
        System.out.println("SM2 解密后的SM4密钥: " + decryptSm4Key);

        // 验证 SM4 密钥是否一致
        boolean keyMatch = password.equals(decryptSm4Key);
        System.out.println("SM4 密钥验证: " + (keyMatch ? "✅ 一致" : "❌ 不一致"));

        // 使用 SM4 密钥解密数据
        String decryptText = decryptBySm4(encryptDataHex, decryptSm4Key);
        System.out.println("SM4 解密后的明文: " + decryptText);

        // 最终验证
        boolean dataMatch = plainText.equals(decryptText);
        System.out.println("数据完整性验证: " + (dataMatch ? "✅ 成功" : "❌ 失败"));
    }
}
