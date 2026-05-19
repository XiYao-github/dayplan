package com.xiyao.encrypt.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

/**
 * 安全相关工具类
 */
public class EncryptUtils {

    /**
     * SM4加密
     *
     * @param data     待加密数据
     * @param password 秘钥字符串
     * @return 加密后字符串
     */
    public static String encryptBySm4(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * SM4加密（Hex编码）
     *
     * @param data     待加密数据
     * @param password 秘钥字符串
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySm4Hex(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * sm4解密
     *
     * @param data     待解密数据（可以是Base64或Hex编码）
     * @param password 秘钥字符串
     * @return 解密后字符串
     */
    public static String decryptBySm4(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length()) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.decryptStr(data, StandardCharsets.UTF_8);
    }

    /**
     * sm2公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串
     */
    public static String encryptBySm2(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new IllegalArgumentException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptBase64(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * sm2公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySm2Hex(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new IllegalArgumentException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptHex(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * sm2私钥解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return 解密后字符串
     */
    public static String decryptBySm2(String data, String privateKey) {
        if (StrUtil.isBlank(privateKey)) {
            throw new IllegalArgumentException("SM2需要传入私钥进行解密");
        }
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.decryptStr(data, KeyType.PrivateKey, StandardCharsets.UTF_8);
    }

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
