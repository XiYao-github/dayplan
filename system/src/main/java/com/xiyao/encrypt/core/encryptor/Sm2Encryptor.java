package com.xiyao.encrypt.core.encryptor;

import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;
import com.xiyao.encrypt.utils.EncryptUtils;

/**
 * SM2 非对称加密器
 * <p>
 * 实现基于国密 SM2 算法的非对称加解密功能。
 * SM2 算法使用公钥加密、私钥解密，适用于加密少量数据或传输密钥。
 * <p>
 * <b>算法特点：</b>
 * <ul>
 *     <li>非对称加密：公钥加密，私钥解密</li>
 *     <li>安全性高：基于椭圆曲线密码学</li>
 *     <li>性能较低：不适合加密大量数据</li>
 * </ul>
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>加密传输对称密钥（如 SM4 密钥）</li>
 *     <li>数字签名</li>
 *     <li>少量敏感数据的加密存储</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * EncryptContext context = new EncryptContext();
 * context.setPublicKey("sm2-public-key");
 * context.setPrivateKey("sm2-private-key");
 *
 * Sm2Encryptor encryptor = new Sm2Encryptor(context);
 * String encrypted = encryptor.encrypt("sensitive-data", EncodeType.HEX);
 * String decrypted = encryptor.decrypt(encrypted);
 * }</pre>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>公钥和私钥必须成对提供，缺一不可</li>
 *     <li>建议配合 SM4 使用：SM2 加密 SM4 密钥，SM4 加密实际数据</li>
 *     <li>密钥长度建议使用 256 位</li>
 * </ul>
 *
 * @author xiyao
 * @see AbstractEncryptor
 * @see AlgorithmType#SM2
 * @see EncryptUtils
 */
public class Sm2Encryptor extends AbstractEncryptor {

    /**
     * 加密上下文引用
     * <p>
     * 保存上下文引用，避免每次调用时都访问父类属性。
     */
    private final EncryptContext context;

    /**
     * 构造方法
     * <p>
     * 创建 SM2 加密器实例，校验公私钥是否有效。
     *
     * @param context 加解密配置上下文，包含公钥和私钥
     * @throws IllegalArgumentException 如果公钥或私钥为空
     */
    public Sm2Encryptor(EncryptContext context) {
        super(context);
        // 获取配置的公钥和私钥
        String publicKey = context.getPublicKey();
        String privateKey = context.getPrivateKey();

        // 校验密钥：SM2 公私钥必须同时提供
        if (StrUtil.isBlank(publicKey) || StrUtil.isBlank(privateKey)) {
            throw new IllegalArgumentException("SM2公私钥均需要提供，公钥加密，私钥解密。");
        }

        this.context = context;
    }

    /**
     * 获取当前算法类型
     *
     * @return 算法类型为 SM2
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM2;
    }

    /**
     * 加密数据
     * <p>
     * 使用 SM2 公钥对数据进行加密，支持 HEX 和 BASE64 两种编码输出。
     *
     * @param value  待加密的明文字符串
     * @param encode 加密后的编码格式
     * @return 加密后的密文字符串
     */
    @Override
    public String encrypt(String value, EncodeType encode) {
        if (encode == EncodeType.HEX) {
            // HEX 编码输出
            return EncryptUtils.encryptBySm2Hex(value, context.getPublicKey());
        } else {
            // BASE64 编码输出
            return EncryptUtils.encryptBySm2(value, context.getPublicKey());
        }
    }

    /**
     * 解密数据
     * <p>
     * 使用 SM2 私钥对数据进行解密。
     *
     * @param value 待解密的密文字符串
     * @return 解密后的明文字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm2(value, context.getPrivateKey());
    }
}