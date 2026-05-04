package com.xiyao.encrypt.core.encryptor;

import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.utils.EncryptUtils;

/**
 * sm2算法实现
 */
public class Sm2Encryptor extends AbstractEncryptor {

    private final EncryptContext context;

    /**
     * 构造方法
     *
     * @param context 加解密配置参数
     */
    public Sm2Encryptor(EncryptContext context) {
        super(context);
        String publicKey = context.getPublicKey();
        String privateKey = context.getPrivateKey();
        if (StrUtil.isBlank(publicKey) || StrUtil.isBlank(privateKey)) {
            throw new IllegalArgumentException("SM2公私钥均需要提供，公钥加密，私钥解密。");
        }
        this.context = context;
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM2;
    }

    /**
     * 加密
     *
     * @param value 待加密字符串
     */
    @Override
    public String encrypt(String value) {
        return EncryptUtils.encryptBySm2(value, context.getPublicKey());
    }

    /**
     * 解密
     *
     * @param value 待加密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm2(value, context.getPrivateKey());
    }
}
