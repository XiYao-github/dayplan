package com.xiyao.encrypt.core;


import com.xiyao.encrypt.enums.AlgorithmType;

/**
 * 加解者
 */
public interface IEncryptor {

    /**
     * 获得当前算法
     */
    AlgorithmType algorithm();

    /**
     * 加密
     *
     * @param value 待加密字符串
     * @return 加密后的字符串
     */
    String encrypt(String value);

    /**
     * 解密
     *
     * @param value 待加密字符串
     * @return 解密后的字符串
     */
    String decrypt(String value);
}
