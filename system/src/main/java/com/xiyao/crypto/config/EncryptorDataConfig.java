package com.xiyao.crypto.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.crypto.interceptor.DecryptInterceptor;
import com.xiyao.crypto.interceptor.EncryptInterceptor;
import com.xiyao.crypto.properties.EncryptorData;
import com.xiyao.crypto.core.EncryptorManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据加解密配置类
 * <p>
 * 配置基于 MyBatis 拦截器的数据库字段加解密功能。
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>启用条件：encryptor-data.enable=true</li>
 *     <li>EncryptInterceptor：入参加密拦截器，数据写入数据库前自动加密</li>
 *     <li>DecryptInterceptor：出参解密拦截器，数据查询出数据库后自动解密</li>
 * </ul>
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>通过 MyBatis-Plus 的 Interceptor 机制拦截 SQL 执行</li>
 *     <li>入参拦截器遍历参数对象，对带有 @CryptoField 注解的字段进行加密</li>
 *     <li>出参拦截器遍历结果集，对带有 @CryptoField 注解的字段进行解密</li>
 *     <li>加解密过程对业务代码透明，无需额外处理</li>
 * </ol>
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   crypto:
 *     data:
 *       enable: true
 *       algorithm: SM4
 *       encode: HEX
 *       password: "your-sm4-password"
 *       publicKey: "sm2-public-key"
 *       privateKey: "sm2-private-key"
 * }</pre>
 *
 * @author xiyao
 * @see EncryptInterceptor
 * @see DecryptInterceptor
 * @see EncryptorManager
 * @see EncryptorData
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorData.class)
@ConditionalOnProperty(value = "system.crypto.data.enable", havingValue = "true")
public class EncryptorDataConfig {

    /**
     * 创建加密管理器
     * <p>
     * EncryptorManager 负责管理加解密上下文、字段缓存等核心功能。
     *
     * @return EncryptorManager 实例
     */
    @Bean
    public EncryptorManager encryptorManager() {
        return new EncryptorManager();
    }

    /**
     * 创建入参加密拦截器
     * <p>
     * 拦截 MyBatis 入参，在数据写入数据库前对带有 @CryptoField 注解的字段进行加密。
     *
     * @param encryptorManager 加密管理器
     * @param properties       配置属性
     * @return EncryptInterceptor 实例
     */
    @Bean
    public EncryptInterceptor encryptInterceptor(EncryptorManager encryptorManager, EncryptorData properties) {
        return new EncryptInterceptor(encryptorManager, properties);
    }

    /**
     * 创建出参解密拦截器
     * <p>
     * 拦截 MyBatis 出参，在数据查询出数据库后对带有 @CryptoField 注解的字段进行解密。
     *
     * @param encryptorManager 加密管理器
     * @param properties       配置属性
     * @return DecryptInterceptor 实例
     */
    @Bean
    public DecryptInterceptor decryptInterceptor(EncryptorManager encryptorManager, EncryptorData properties) {
        return new DecryptInterceptor(encryptorManager, properties);
    }

}