package com.xiyao.encrypt.enums;

import com.xiyao.encrypt.core.encryptor.AbstractEncryptor;
import com.xiyao.encrypt.core.encryptor.Base64Encryptor;
import com.xiyao.encrypt.core.encryptor.Sm2Encryptor;
import com.xiyao.encrypt.core.encryptor.Sm4Encryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 算法名称
 */
@Getter
@AllArgsConstructor
public enum AlgorithmType {

    /**
     * yml配置
     */
    DEFAULT(null),

    /**
     * base64
     */
    BASE64(Base64Encryptor.class),

    /**
     * sm4
     */
    SM4(Sm4Encryptor.class),

    /**
     * sm2
     */
    SM2(Sm2Encryptor.class);

    private final Class<? extends AbstractEncryptor> clazz;

}
