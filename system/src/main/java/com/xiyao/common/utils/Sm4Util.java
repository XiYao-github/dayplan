package com.xiyao.common.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class Sm4Util {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ENCRYPT_PREFIX = "ENC_";
    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/ECB/PKCS7Padding";
    private static final byte[] SECRET_KEY = "1234567890123456".getBytes();

    /**
     * 加密（自动添加标识前缀）
     *
     * @param plainText 明文
     * @return 带标识的密文（ENC:xxx）或原文（如果为空）
     */
    public static String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            String cipherText = Hex.toHexString(encrypted);
            // 添加标识前缀
            return ENCRYPT_PREFIX + cipherText;
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }

    /**
     * 解密（自动判断是否有标识）
     *
     * @param cipherText 带标识的密文（ENC:xxx）或普通字符串
     * @return 解密后的明文或原文
     */
    public static String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText)) {
            return cipherText;
        }

        // 判断是否有加密标识
        if (cipherText.startsWith(ENCRYPT_PREFIX)) {
            try {
                // 去掉标识前缀，获取真正的密文
                String realCipherText = cipherText.substring(ENCRYPT_PREFIX.length());
                byte[] cipherBytes = Hex.decode(realCipherText);
                SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, ALGORITHM);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] decrypted = cipher.doFinal(cipherBytes);
                return new String(decrypted);
            } catch (Exception e) {
                throw new RuntimeException("SM4解密失败", e);
            }
        }

        // 没有标识，说明是明文，直接返回
        return cipherText;
    }

    /**
     * 判断一个字符串是否是加密数据
     */
    public static boolean isEncrypted(String text) {
        return StringUtils.hasText(text) && text.startsWith(ENCRYPT_PREFIX);
    }
}