package com.xiyao.encrypt.enums;

import com.xiyao.encrypt.core.encryptor.AbstractEncryptor;
import com.xiyao.encrypt.core.encryptor.Sm2Encryptor;
import com.xiyao.encrypt.core.encryptor.Sm4Encryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 加密算法类型枚举
 * <p>
 * 定义系统支持的加密算法，包括 SM2（非对称）和 SM4（对称）两种国密算法。
 * <p>
 * <b>算法对比：</b>
 * <table border="1">
 *     <tr><th>算法</th><th>类型</th><th>密钥</th><th>适用场景</th></tr>
 *     <tr><td>SM2</td><td>非对称</td><td>公钥+私钥</td><td>密钥交换、数字签名</td></tr>
 *     <tr><td>SM4</td><td>对称</td><td>单一密钥</td><td>大量数据加密</td></tr>
 * </table>
 * <p>
 * <b>设计思路：</b>
 * <ul>
 *     <li>使用策略模式：通过枚举值选择不同的加密器实现</li>
 *     <li>DEFAULT 枚举值：表示使用配置文件中的默认算法</li>
 *     <li>每个枚举值关联对应的加密器 Class，用于动态创建实例</li>
 * </ul>
 *
 * @author xiyao
 * @see AbstractEncryptor
 * @see Sm2Encryptor
 * @see Sm4Encryptor
 */
@Getter
@AllArgsConstructor
public enum AlgorithmType {

    /**
     * 默认算法
     * <p>
     * 使用 yml 配置文件中的全局算法配置，由 EncryptorManager 根据配置创建对应的加密器。
     */
    DEFAULT(null),

    /**
     * SM4 对称加密算法
     * <p>
     * 国产对称加密算法，适用于大量数据的加解密。
     */
    SM4(Sm4Encryptor.class),

    /**
     * SM2 非对称加密算法
     * <p>
     * 国产非对称加密算法，基于椭圆曲线，适用于密钥交换和数字签名。
     */
    SM2(Sm2Encryptor.class);

    /**
     * 对应加密器类
     * <p>
     * 存储该算法类型对应的加密器实现类，供策略模式使用。
     * DEFAULT 类型为 null，因为具体算法由配置决定。
     */
    private final Class<? extends AbstractEncryptor> clazz;

}