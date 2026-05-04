package com.xiyao.encrypt.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiyao.encrypt.annotation.EncryptField;
import com.xiyao.encrypt.core.EncryptContext;
import com.xiyao.encrypt.core.EncryptorManager;
import com.xiyao.encrypt.enums.AlgorithmType;
import com.xiyao.encrypt.enums.EncodeType;
import com.xiyao.encrypt.properties.EncryptorData;
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
 */
@Slf4j
@Intercepts({@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class})
})
@AllArgsConstructor
public class DecryptInterceptor implements Interceptor {

    private final EncryptorManager encryptorManager;
    private final EncryptorData properties;

    /**
     * 拦截器方法
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获得查询结果
        Object result = invocation.proceed();
        this.decryptHandler(result);
        // 解密处理（核心功能）
        return result;
    }

    /**
     * 递归解密对象
     */
    private void decryptHandler(Object result) {
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
        Set<Field> fields = this.encryptorManager.getFieldCache(result.getClass());
        // 没有需要解密的字段，直接返回
        if (CollUtil.isEmpty(fields)) {
            return;
        }
        try {
            // 遍历加密字段，解密后重新设置
            for (Field field : fields) {
                String encryptField = Convert.toStr(field.get(result));
                String decryptField = this.decryptField(encryptField, field);
                field.set(result, decryptField);
            }
        } catch (Exception e) {
            log.error("字段解密处理出错", e);
        }
    }

    /**
     * 字段值进行解密
     */
    private String decryptField(String value, Field field) {
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        // 获取注解配置信息
        EncryptField encryptField = field.getAnnotation(EncryptField.class);
        EncryptContext encryptContext = new EncryptContext();
        encryptContext.setAlgorithm(encryptField.algorithm() == AlgorithmType.DEFAULT ? properties.getAlgorithm() : encryptField.algorithm());
        encryptContext.setEncode(encryptField.encode() == EncodeType.DEFAULT ? properties.getEncode() : encryptField.encode());
        encryptContext.setPassword(StrUtil.isBlank(encryptField.password()) ? properties.getPassword() : encryptField.password());
        encryptContext.setPrivateKey(StrUtil.isBlank(encryptField.privateKey()) ? properties.getPrivateKey() : encryptField.privateKey());
        encryptContext.setPublicKey(StrUtil.isBlank(encryptField.publicKey()) ? properties.getPublicKey() : encryptField.publicKey());
        return this.encryptorManager.decrypt(value, encryptContext);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
