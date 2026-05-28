package com.xiyao.crypto.core;


import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;

/**
 * 加解密器接口
 * <p>
 * 定义加解密器的通用接口，所有具体加密算法实现都必须实现此接口。
 * <p>
 * <b>设计模式：</b>
 * 策略模式（Strategy Pattern）- 通过实现此接口可以在运行时切换不同的加密算法。
 *
 * <p>
 * <b>实现类：</b>
 * <ul>
 *     <li>Sm2Encryptor：SM2 非对称加密器</li>
 *     <li>Sm4Encryptor：SM4 对称加密器</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.crypto.core.encryptor.Sm2Encryptor
 * @see com.xiyao.crypto.core.encryptor.Sm4Encryptor
 */
public interface IEncryptor {

    /**
     * 获取当前加密算法类型
     * <p>
     * 返回该加密器实例使用的算法类型。
     *
     * @return 算法类型枚举
     */
    AlgorithmType algorithm();

    /**
     * 加密数据
     * <p>
     * 使用当前加密算法对明文进行加密，支持指定输出编码格式。
     *
     * @param value  待加密的明文字符串
     * @param encode 加密后的编码格式（Base64 或 HEX）
     * @return 加密后的字符串
     */
    String encrypt(String value, EncodeType encode);

    /**
     * 解密数据
     * <p>
     * 使用当前加密算法对密文进行解密，还原为明文。
     *
     * @param value 待解密的密文字符串
     * @return 解密后的明文字符串
     */
    String decrypt(String value);
}
