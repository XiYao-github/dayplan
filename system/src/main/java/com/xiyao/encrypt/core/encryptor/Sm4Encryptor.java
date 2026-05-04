package com.xiyao.encrypt.core.encryptor;

import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.utils.EncryptUtils;

/**
 * sm4算法实现
 */
public class Sm4Encryptor extends AbstractEncryptor {

    private final EncryptContext context;

    /**
     * 构造方法
     *
     * @param context 加解密配置参数
     */
    public Sm4Encryptor(EncryptContext context) {
        super(context);
        String password = context.getPassword();
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要提供安全秘钥。");
        }
        this.context = context;
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM4;
    }

    /**
     * 加密
     *
     * @param value 待加密字符串
     */
    @Override
    public String encrypt(String value) {
        return EncryptUtils.encryptBySm4(value, context.getPassword());
    }

    /**
     * 解密
     *
     * @param value 待加密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm4(value, context.getPassword());
    }
}
