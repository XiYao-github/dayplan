package com.xiyao.encrypt.core.encryptor;

import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;
import com.xiyao.encrypt.utils.EncryptUtils;

/**
 * SM4 对称加密器
 * <p>
 * 实现基于国密 SM4 算法的对称加解密功能。
 * SM4 算法使用相同密钥进行加密和解密，适用于大量数据的加密场景。
 * <p>
 * <b>算法特点：</b>
 * <ul>
 *     <li>对称加密：加密和解密使用相同的密钥</li>
 *     <li>分组密码：128 位分组，128/192/256 位密钥</li>
 *     <li>性能高：适合加密大量数据</li>
 * </ul>
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>数据库字段加密存储</li>
 *     <li>文件加密</li>
 *     <li>API 请求/响应加密（通常与 SM2 结合使用）</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * EncryptContext context = new EncryptContext();
 * context.setPassword("your-16-char-password");
 *
 * Sm4Encryptor encryptor = new Sm4Encryptor(context);
 * String encrypted = encryptor.encrypt("sensitive-data", EncodeType.HEX);
 * String decrypted = encryptor.decrypt(encrypted);
 * }</pre>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>密钥长度必须为 16 字符（128 位）</li>
 *     <li>建议使用随机字符串作为密钥，不要使用简单密码</li>
 *     <li>密钥需要安全存储，不要硬编码在代码中</li>
 * </ul>
 *
 * @author xiyao
 * @see AbstractEncryptor
 * @see AlgorithmType#SM4
 * @see EncryptUtils
 */
public class Sm4Encryptor extends AbstractEncryptor {

    /**
     * 加密上下文引用
     * <p>
     * 保存上下文引用，避免每次调用时都访问父类属性。
     */
    private final EncryptContext context;

    /**
     * 构造方法
     * <p>
     * 创建 SM4 加密器实例，校验密钥是否有效。
     *
     * @param context 加解密配置上下文，包含密钥
     * @throws IllegalArgumentException 如果密钥为空
     */
    public Sm4Encryptor(EncryptContext context) {
        super(context);
        // 获取配置的密钥
        String password = context.getPassword();

        // 校验密钥：SM4 必须提供密钥
        if (StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("SM4需要提供安全秘钥。");
        }

        this.context = context;
    }

    /**
     * 获取当前算法类型
     *
     * @return 算法类型为 SM4
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM4;
    }

    /**
     * 加密数据
     * <p>
     * 使用 SM4 密钥对数据进行加密，支持 HEX 和 BASE64 两种编码输出。
     *
     * @param value  待加密的明文字符串
     * @param encode 加密后的编码格式
     * @return 加密后的密文字符串
     */
    @Override
    public String encrypt(String value, EncodeType encode) {
        if (encode == EncodeType.HEX) {
            // HEX 编码输出
            return EncryptUtils.encryptBySm4Hex(value, context.getPassword());
        } else {
            // BASE64 编码输出
            return EncryptUtils.encryptBySm4(value, context.getPassword());
        }
    }

    /**
     * 解密数据
     * <p>
     * 使用 SM4 密钥对数据进行解密。
     *
     * @param value 待解密的密文字符串
     * @return 解密后的明文字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm4(value, context.getPassword());
    }
}