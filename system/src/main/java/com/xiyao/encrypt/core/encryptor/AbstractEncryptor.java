package com.xiyao.encrypt.core.encryptor;

import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.core.IEncryptor;
import lombok.AllArgsConstructor;

/**
 * 加密执行者基类
 */
@AllArgsConstructor
public abstract class AbstractEncryptor implements IEncryptor {

    /**
     * 加解密配置参数
     */
    private final EncryptContext context;

}
