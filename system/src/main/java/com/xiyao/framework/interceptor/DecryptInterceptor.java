package com.xiyao.framework.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.xiyao.common.utils.Sm4Util;
import com.xiyao.framework.annotation.EncryptField;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

@Intercepts({
        @Signature(
                type = ResultSetHandler.class,
                method = "handleResultSets",
                args = {Statement.class}
        )
})
public class DecryptInterceptor implements Interceptor  {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return decrypt(invocation.proceed());
    }

    /**
     * 数据库查询：密文 → 明文
     */
    private Object decrypt(Object result) {
        if (result == null) {
            return result;
        }

        // 集合处理
        if (result instanceof Collection) {
            ((Collection<?>) result).forEach(this::decrypt);
            return result;
        }

        // 基本类型处理
        if (result instanceof String || result instanceof Number) {
            return result;
        }

        // 对象处理
        Arrays.stream(result.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EncryptField.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        String value = (String) field.get(result);
                        if (value != null && !value.isEmpty()) {
                            field.set(result, Sm4Util.decrypt(value));
                        }
                    } catch (Exception ignored) {
                    }
                });

        return result;
    }


}