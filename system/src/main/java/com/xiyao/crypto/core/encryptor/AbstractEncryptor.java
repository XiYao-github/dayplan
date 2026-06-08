package com.xiyao.crypto.core.encryptor;

import com.xiyao.crypto.core.EncryptContext;
import com.xiyao.crypto.core.IEncryptor;
import lombok.RequiredArgsConstructor;

/**
 * 加密执行器基类
 * <p>
 * 定义加密器的通用结构和行为，所有具体加密算法实现都应继承此类。
 * <p>
 * <b>设计模式：</b>
 * <ul>
 *     <li>模板方法模式：定义加密/解密算法骨架，子类实现具体算法细节</li>
 *     <li>策略模式：通过 AlgorithmType 枚举选择不同的加密器实现</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 创建加密上下文
 * EncryptContext context = new EncryptContext();
 * context.setPassword("your-sm4-password");
 *
 * // 使用 Sm4Encryptor
 * Sm4Encryptor encryptor = new Sm4Encryptor(context);
 * String encrypted = encryptor.encrypt("sensitive-data", EncodeType.HEX);
 * String decrypted = encryptor.decrypt(encrypted);
 * }</pre>
 *
 * @author xiyao
 * @see IEncryptor
 * @see EncryptContext
 * @see Sm2Encryptor
 * @see Sm4Encryptor
 */
@RequiredArgsConstructor
public abstract class AbstractEncryptor implements IEncryptor {

    /**
     * 加解密配置上下文
     * <p>
     * 包含加密所需的配置信息，如密钥、编码方式、算法类型等。
     * 由子类构造器初始化，存储为 final 保证不可变。
     */
    protected final EncryptContext context;

}