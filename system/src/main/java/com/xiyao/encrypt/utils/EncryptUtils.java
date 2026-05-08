package com.xiyao.encrypt.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;

import javax.crypto.SecretKey;
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
        if (password.length() != 16) {
            throw new IllegalArgumentException("SM4秘钥长度要求为16位");
        }
        SM4 sm4 = SmUtil.sm4(password.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * sm4解密
     *
     * @param data     待解密数据
     * @param password 秘钥字符串
     * @return 解密后字符串
     */
    public static String decryptBySm4(String data, String password) {
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        if (password.length() != 16) {
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
        System.out.println("========== SM4 测试 ==========");
        SecretKey secretKey1 = KeyUtil.generateKey("SM4");
        SecretKey secretKey2 = SmUtil.sm4().getSecretKey();
        // 转为Hex字符串（32位十六进制）
        String hexKey1 = HexUtil.encodeHexStr(secretKey1.getEncoded());
        String hexKey2 = HexUtil.encodeHexStr(secretKey2.getEncoded());
        System.out.println("SM4 密钥(Hex): " + hexKey1);
        System.out.println("SM4 密钥(Hex): " + hexKey2);


        System.out.println("\n========== SM2 测试 ==========");
        KeyPair keyPair = KeyUtil.generateKeyPair("SM2");
        SM2 sm2 = SmUtil.sm2();
        String hexKey3 = HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(keyPair.getPrivate()));
        String hexKey4 = HexUtil.encodeHexStr(BCUtil.encodeECPublicKey(keyPair.getPublic()));
        System.out.println("SM2 私钥(Hex): " + hexKey3);
        System.out.println("SM2 公钥(Hex): " + hexKey4);

    }

}
