package com.xiyao.dict.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.dict.annotation.DictBind;
import com.xiyao.dict.utils.DictCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * MyBatis 查询结果字典回显拦截器
 * <p>
 * 在查询数据库后将带有 @DictBind 注解的字段自动填充对应的字典标签文本。
 *
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>MyBatis 执行 SQL 查询，返回结果集</li>
 *     <li>拦截器截获查询结果，遍历结果对象</li>
 *     <li>对每个对象获取所有带 @DictBind 注解的字段</li>
 *     <li>根据字典编码和字段值从 DictCache 获取字典标签</li>
 *     <li>将标签文本填充到 target 字段</li>
 * </ol>
 *
 * <p>
 * <b>支持的数据类型：</b>
 * <ul>
 *     <li>单个对象：如 User、Order 等实体类</li>
 *     <li>集合类型：如 List&lt;User&gt;、Set&lt;Order&gt;</li>
 *     <li>Map 类型：如 Map&lt;String, Object&gt;</li>
 * </ul>
 *
 * <p>
 * <b>注解使用：</b>
 * <pre>{@code
 * public class UserVO {
 *     @DictBind(code = "status", target = "statusText")
 *     private Integer status;
 *     private String statusText;  // 自动填充为"正常"或"暂停"
 * }
 * }</pre>
 *
 * @author xiyao
 * @see DictBind
 * @see DictCache
 */
@Slf4j
@Intercepts({@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class})
})
public class DictInterceptor implements Interceptor {

    /**
     * 拦截器核心方法
     * <p>
     * 在 MyBatis 查询结果集处理完成后执行字典回显逻辑。
     *
     * @param invocation 调用信息
     * @return 原始查询结果（字典回显在原对象上直接修改）
     * @throws Throwable 如果处理过程发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 执行原查询逻辑，获取查询结果
        Object result = invocation.proceed();

        // 对查询结果进行递归字典回显处理
        this.processDict(result);

        // 返回结果（对象引用已修改，无需替换）
        return result;
    }

    /**
     * 递归字典回显处理
     * <p>
     * 对查询结果进行深度遍历，处理不同类型的对象结构。
     * <ul>
     *     <li>Map 类型：遍历所有 value 并递归处理</li>
     *     <li>Collection 类型：遍历所有元素递归处理</li>
     *     <li>普通对象：获取类所有 @DictBind 字段并处理</li>
     * </ul>
     *
     * @param result 查询结果对象
     */
    private void processDict(Object result) {
        // 空值检查，避免空指针异常
        if (ObjectUtil.isEmpty(result)) {
            return;
        }

        // 处理 Map 类型：遍历所有 value 并递归处理
        if (result instanceof Map<?, ?> map) {
            Collection<?> values = map.values();
            values.forEach(this::processDict);
            return;
        }

        // 处理 Collection 类型：遍历所有元素递归处理
        if (result instanceof Collection<?> collection) {
            collection.forEach(this::processDict);
            return;
        }

        // 处理普通对象：获取当前类声明的所有字段
        Class<?> aClass = result.getClass();
        Field[] fields = aClass.getDeclaredFields();

        try {
            for (Field field : fields) {
                // 只处理带有 @DictBind 注解的字段
                if (field.isAnnotationPresent(DictBind.class)) {
                    processDictField(field, result);
                }
            }
        } catch (Exception e) {
            // 记录处理错误，但不影响业务逻辑继续执行
            log.error("数据字典处理出错", e);
        }
    }

    /**
     * 处理字典字段回显
     * <p>
     * 根据 @DictBind 注解的 code 和字段值，从字典缓存获取对应的标签文本，
     * 填充到 target 字段。
     * 使用 MetaObject 自动处理字段可访问性，target 字段不存在时安全跳过。
     *
     * @param field  字段对象，用于获取注解配置
     * @param result 目标对象，字段值从该对象获取
     */
    private void processDictField(Field field, Object result) {
        // 获取字段上的 @DictBind 注解
        DictBind dictBind = field.getAnnotation(DictBind.class);
        if (dictBind == null) {
            return;
        }

        // 获取字典编码
        String code = dictBind.code();

        // target 必须显式指定，否则跳过处理
        String target = dictBind.target();
        if (target == null || target.isEmpty()) {
            return;
        }

        // 使用 MetaObject 获取和设置值，自动处理字段可访问性
        MetaObject metaObject = SystemMetaObject.forObject(result);

        // 获取字段值
        Object value = metaObject.getValue(field.getName());
        if (value == null || value.toString().isEmpty()) {
            return;
        }

        // 从字典缓存获取标签文本
        String label = DictCache.getInstance().getDictLabel(code, value.toString());
        if (label == null || label.isEmpty()) {
            return;
        }

        // 设置 target 字段值（MetaObject 自动处理 accessible，字段不存在安全跳过）
        metaObject.setValue(target, label);
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