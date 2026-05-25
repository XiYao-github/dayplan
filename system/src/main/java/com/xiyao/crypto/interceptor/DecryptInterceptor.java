package com.xiyao.crypto.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.crypto.annotation.CryptoField;
import com.xiyao.crypto.core.EncryptContext;
import com.xiyao.crypto.enums.AlgorithmType;
import com.xiyao.crypto.enums.EncodeType;
import com.xiyao.crypto.properties.EncryptorData;
import com.xiyao.crypto.core.EncryptorManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 出参解密拦截器
 * <p>
 * MyBatis-Plus 拦截器，用于在查询数据库后将带有 @CryptoField 注解的字段自动解密。
 * 配合 EncryptorManager 实现数据出库时的透明解密功能。
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>MyBatis 执行 SQL 查询，返回结果集</li>
 *     <li>拦截器截获查询结果，遍历结果对象</li>
 *     <li>对每个对象获取所有带 @CryptoField 注解的字段</li>
 *     <li>根据字段注解配置调用对应的解密器进行解密</li>
 *     <li>将解密后的值重新设置到对象字段中</li>
 * </ol>
 * <p>
 * <b>支持的数据类型：</b>
 * <ul>
 *     <li>单个对象：如 User、Order 等实体类</li>
 *     <li>集合类型：如 List&lt;User&gt;、Set&lt;Order&gt;</li>
 *     <li>Map 类型：如 Map&lt;String, Object&gt;</li>
 * </ul>
 * <p>
 * <b>优先级：</b>
 * 字段注解配置优先于全局配置，即：
 * <ul>
 *     <li>algorithm：字段注解 &gt; yml 全局配置</li>
 *     <li>encode：字段注解 &gt; yml 全局配置</li>
 *     <li>password/privateKey/publicKey：字段注解 &gt; yml 全局配置</li>
 * </ul>
 *
 * @author xiyao
 * @see EncryptInterceptor
 * @see com.xiyao.crypto.core.EncryptorManager
 * @see CryptoField
 */
@Slf4j
@Intercepts({@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class})
})
@AllArgsConstructor
public class DecryptInterceptor implements Interceptor {

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
     * 拦截器核心方法
     * <p>
     * 在 MyBatis 查询结果集处理完成后执行解密逻辑。
     *
     * @param invocation 调用信息
     * @return 原始查询结果（解密在原对象上直接修改）
     * @throws Throwable 如果解密过程发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 第1步：执行原查询逻辑，获取查询结果
        Object result = invocation.proceed();

        // 第2步：对查询结果进行递归解密处理
        this.decryptHandler(result);

        // 第3步：返回结果（对象引用已修改，无需替换）
        return result;
    }

    /**
     * 递归解密处理
     * <p>
     * 对查询结果进行深度遍历，处理不同类型的对象结构。
     * <ul>
     *     <li>Map 类型：遍历所有 value 并递归解密</li>
     *     <li>Collection 类型：遍历所有元素递归解密</li>
     *     <li>普通对象：获取类所有加密字段并解密</li>
     * </ul>
     *
     * @param result 查询结果对象
     */
    private void decryptHandler(Object result) {
        // 空值检查，避免空指针异常
        if (ObjectUtil.isEmpty(result)) {
            return;
        }

        // 处理 Map 类型：遍历所有 value 并递归解密
        if (result instanceof Map<?, ?> map) {
            Collection<?> values = map.values();
            values.forEach(this::decryptHandler);
            return;
        }

        // 处理 Collection 类型：遍历所有元素递归解密
        if (result instanceof Collection<?> collection) {
            collection.forEach(this::decryptHandler);
            return;
        }

        // 处理普通对象：获取类所有加密字段
        Set<Field> fields = this.manager.getFieldCache(result.getClass());

        // 没有需要解密的字段，直接返回
        if (CollUtil.isEmpty(fields)) {
            return;
        }

        try {
            // 遍历所有加密字段，逐个进行解密
            for (Field field : fields) {
                // 获取字段当前值（加密状态）
                String encryptField = Convert.toStr(field.get(result));

                // 对字段值进行解密
                String decryptField = this.decryptField(encryptField, field);

                // 将解密后的值重新设置到字段
                field.set(result, decryptField);
            }
        } catch (Exception e) {
            // 记录解密错误，但不影响业务逻辑继续执行
            log.error("字段解密处理出错", e);
        }
    }

    /**
     * 字段值解密
     * <p>
     * 根据字段注解配置和全局配置，构建解密上下文并执行解密。
     * <p>
     * <b>配置优先级：</b>
     * 字段注解配置优先于全局配置，如果字段注解值为默认值，则使用全局配置。
     *
     * @param value 加密的字段值
     * @param field 字段对象，用于获取注解配置
     * @return 解密后的明文值
     */
    private String decryptField(String value, Field field) {
        // 空值检查
        if (StrUtil.isBlank(value)) {
            return null;
        }

        // 获取字段的加密注解配置
        CryptoField cryptoField = field.getAnnotation(CryptoField.class);

        // 构建解密上下文
        EncryptContext context = new EncryptContext();

        // 设置算法类型：字段注解优先，否则使用全局配置
        context.setAlgorithm(cryptoField.algorithm() == AlgorithmType.DEFAULT
                ? properties.getAlgorithm() : cryptoField.algorithm());

        // 设置编码类型：字段注解优先，否则使用全局配置
        context.setEncode(cryptoField.encode() == EncodeType.DEFAULT
                ? properties.getEncode() : cryptoField.encode());

        // 设置密钥：字段注解优先，否则使用全局配置
        context.setPassword(StrUtil.isBlank(cryptoField.password())
                ? properties.getPassword() : cryptoField.password());

        // 设置私钥：字段注解优先，否则使用全局配置
        context.setPrivateKey(StrUtil.isBlank(cryptoField.privateKey())
                ? properties.getPrivateKey() : cryptoField.privateKey());

        // 设置公钥：字段注解优先，否则使用全局配置
        context.setPublicKey(StrUtil.isBlank(cryptoField.publicKey())
                ? properties.getPublicKey() : cryptoField.publicKey());

        // 委托给加密管理器执行解密
        return this.manager.decrypt(value, context);
    }

    /**
     * 将拦截器包装为 MyBatis 插件
     * <p>
     * MyBatis 通过此方法将拦截器注册到拦截器链中。
     *
     * @param target 被拦截的目标对象
     * @return 包装后的代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置插件配置属性
     * <p>
     * 从 MyBatis 配置中读取插件配置参数（当前未使用）。
     *
     * @param properties 配置属性
     */
    @Override
    public void setProperties(Properties properties) {
        // 未使用配置参数
    }

}