package com.xiyao.crypto.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.crypto.annotation.CryptoField;
import com.xiyao.crypto.core.EncryptContext;
import com.xiyao.crypto.core.EncryptorManager;
import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;
import com.xiyao.crypto.properties.EncryptorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 入参加密拦截器
 * <p>
 * 职责：
 * <ul>
 *     <li>拦截 MyBatis 的 ParameterHandler.setParameters() 方法</li>
 *     <li>对带有 @CryptoField 注解的字段进行加密处理</li>
 *     <li>支持 Map、Collection、嵌套对象等多种参数类型</li>
 * </ul>
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>MyBatis 执行 SQL 前触发拦截</li>
 *     <li>获取待设置的参数对象</li>
 *     <li>递归遍历对象树，找到所有带 @CryptoField 的字段</li>
 *     <li>调用 EncryptorManager 加密字段值</li>
 *     <li>将加密后的值重新设置到字段</li>
 *     <li>继续执行 SQL（此时字段值已是密文）</li>
 * </ol>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     @EncryptField
 *     private String idCard;  // 加密存储
 *
 *     @EncryptField(algorithm = AlgorithmType.SM4)
 *     private String bankCard;  // 指定 SM4 算法加密
 * }
 * }</pre>
 *
 * @author xiyao
 * @see EncryptorManager
 * @see CryptoField
 */
@Slf4j
@Intercepts({@Signature(
        type = ParameterHandler.class,
        method = "setParameters",
        args = {PreparedStatement.class})
})
@RequiredArgsConstructor
public class EncryptInterceptor implements Interceptor {

    /**
     * 加密管理器
     * <p>
     * 负责管理加密器实例和字段缓存。
     */
    private final EncryptorManager manager;

    /**
     * 加解密配置属性
     * <p>
     * 从 yml 配置文件中加载的全局加解密配置。
     */
    private final EncryptorData properties;

    /**
     * 拦截 SQL 参数设置操作
     * <p>
     * 在 MyBatis 执行 SQL 前，对参数对象中带 @CryptoField 注解的字段进行加密。
     *
     * @param invocation 调用信息，包含目标对象和方法参数
     * @return 继续执行调用链的结果
     * @throws Throwable 执行过程中的异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof ParameterHandler parameterHandler) {
            // 获取 MyBatis 封装后的参数对象
            Object parameterObject = parameterHandler.getParameterObject();
            // 非空、非字符串类型才进行加密处理（字符串类型不处理）
            if (ObjectUtil.isNotEmpty(parameterObject) && !(parameterObject instanceof String)) {
                // 执行加密处理
                this.encryptHandler(parameterObject);
            }
        }
        return invocation.proceed();
    }

    /**
     * 递归处理对象进行加密
     * <p>
     * 遍历对象树，对所有带 @CryptoField 注解的 String 类型字段进行加密。
     * 支持递归处理 Map、Collection、嵌套对象等复杂结构。
     *
     * @param result 待处理的对象
     */
    private void encryptHandler(Object result) {
        // 空值检查，避免空指针异常
        if (ObjectUtil.isEmpty(result)) {
            return;
        }
        // 处理 Map 类型：遍历所有 value 并递归加密
        if (result instanceof Map<?, ?> map) {
            Collection<?> values = map.values();
            values.forEach(this::encryptHandler);
            return;
        }
        // 处理 Collection 类型：遍历所有元素递归加密
        if (result instanceof Collection<?> collection) {
            collection.forEach(this::encryptHandler);
            return;
        }
        // 处理普通对象：获取类所有加密字段
        Set<Field> fields = this.manager.getFieldCache(result.getClass());
        // 没有需要加密的字段，直接返回
        if (CollUtil.isEmpty(fields)) {
            return;
        }
        try {
            // 遍历所有加密字段，加密后重新设置
            for (Field field : fields) {
                // 获取字段当前值
                String encryptField = Convert.toStr(field.get(result));
                // 执行加密
                String encrypted = this.encryptField(encryptField, field);
                // 重新设置加密后的值
                field.set(result, encrypted);
            }
        } catch (Exception e) {
            log.error("字段加密处理出错", e);
        }
    }

    /**
     * 对单个字段值进行加密
     * <p>
     * 从 @CryptoField 注解获取加密配置（算法、密钥等），
     * 若注解未指定则使用全局配置作为默认值。
     *
     * @param value 待加密的字段值
     * @param field 字段对象，用于获取注解配置
     * @return 加密后的字符串（带 ENC_ 前缀）
     */
    private String encryptField(String value, Field field) {
        // 空值检查
        if (StrUtil.isBlank(value)) {
            return null;
        }

        // 获取注解配置信息
        CryptoField cryptoField = field.getAnnotation(CryptoField.class);

        // 构建加密上下文，注解未配置时使用全局配置
        EncryptContext context = new EncryptContext();

        // 设置算法类型：字段注解优先，否则使用全局配置
        context.setAlgorithm(cryptoField.algorithm() == AlgorithmType.DEFAULT ? properties.getAlgorithm() : cryptoField.algorithm());

        // 设置编码类型：字段注解优先，否则使用全局配置
        context.setEncode(cryptoField.encode() == EncodeType.DEFAULT ? properties.getEncode() : cryptoField.encode());

        // 设置密钥：字段注解优先，否则使用全局配置
        context.setPassword(StrUtil.isBlank(cryptoField.password()) ? properties.getPassword() : cryptoField.password());

        // 设置私钥：字段注解优先，否则使用全局配置
        context.setPrivateKey(StrUtil.isBlank(cryptoField.privateKey()) ? properties.getPrivateKey() : cryptoField.privateKey());

        // 设置公钥：字段注解优先，否则使用全局配置
        context.setPublicKey(StrUtil.isBlank(cryptoField.publicKey()) ? properties.getPublicKey() : cryptoField.publicKey());

        // 执行加密并返回
        return this.manager.encrypt(value, context);
    }

    /**
     * 将拦截器包装到目标对象
     * <p>
     * MyBatis 插件机制的标准实现，使用 Plugin.wrap() 为目标对象创建代理。
     *
     * @param target 目标对象（ParameterHandler）
     * @return 包装后的代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置插件配置属性
     * <p>
     * 从配置文件读取插件初始化参数，当前实现为空。
     *
     * @param properties 插件配置属性
     */
    @Override
    public void setProperties(Properties properties) {
        // 暂不使用插件配置
    }
}
