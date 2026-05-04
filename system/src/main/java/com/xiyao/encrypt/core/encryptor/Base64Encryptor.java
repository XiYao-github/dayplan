package com.xiyao.encrypt.core.encryptor;

import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.utils.EncryptUtils;

/**
 * Base64算法实现
 */
public class Base64Encryptor extends AbstractEncryptor {

    /**
     * 构造方法
     *
     * @param context 加解密配置参数
     */
    public Base64Encryptor(EncryptContext context) {
        super(context);
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.BASE64;
    }

    /**
     * 加密
     *
     * @param value 待加密字符串
     */
    @Override
    public String encrypt(String value) {
        return EncryptUtils.encryptByBase64(value);
    }

    /**
     * 解密
     *
     * @param value 待加密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptByBase64(value);
    }
}
