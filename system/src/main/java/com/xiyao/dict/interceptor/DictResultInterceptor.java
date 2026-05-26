package com.xiyao.dict.interceptor;

import com.xiyao.dict.annotation.DictBind;
import com.xiyao.dict.config.DictCache;
import com.xiyao.dict.enums.BaseEnum;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * MyBatis 查询结果字典回显拦截器
 * <p>
 * 功能说明：
 * <ol>
 *     <li>处理 @DictBind 注解：将字典值填充为字典描述文本</li>
 *     <li>处理枚举类型字段：将数据库存储的值转换为对应的枚举对象</li>
 * </ol>
 *
 * <p>
 * <b>@DictBind 注解说明：</b>
 * <ul>
 *     <li>标注在实体字段上，指定该字段为字典值字段</li>
 *     <li>自动查询字典表，将对应的描述文本填充到 target 字段</li>
 *     <li>target 字段默认使用 原字段名 + "Text"</li>
 * </ul>
 *
 * <p>
 * <b>枚举转换说明：</b>
 * <ul>
 *     <li>自动处理继承 BaseEnum 的枚举类型字段</li>
 *     <li>根据字段类型从数据库读取对应值并转换为枚举</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class UserVO {
 *     @DictBind(code = "status")
 *     private Integer status;
 *     private String statusText;  // 自动填充为"正常"或"暂停"
 *
 *     private DataStatus status;  // 自动转换为枚举
 * }
 * }</pre>
 *
 * @author xiyao
 * @see DictBind
 * @see BaseEnum
 * @see DictCache
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class DictResultInterceptor implements org.apache.ibatis.plugin.Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DictResultInterceptor.class);

    /**
     * 拦截查询结果，进行字典回显和枚举转换处理
     * <p>
     * MyBatis 会自动调用此方法拦截查询结果。
     *
     * @param invocation MyBatis 拦截调用信息
     * @return 处理后的查询结果
     * @throws Throwable 如果处理过程发生异常
     */
    @Override
    public Object intercept(org.apache.ibatis.plugin.Invocation invocation) throws Throwable {
        // 先执行原生的结果处理
        Object result = invocation.proceed();
        if (result == null) {
            return result;
        }
        // 对结果进行字典回显和枚举转换处理
        return processResult(result);
    }

    /**
     * 处理查询结果
     * <p>
     * 区分集合、简单类型和对象类型分别处理。
     * 简单类型（String、Number）直接返回，不做处理。
     *
     * @param result 原始查询结果
     * @return 处理后的结果
     */
    private Object processResult(Object result) {
        if (result instanceof Collection) {
            // 处理集合类型：遍历处理每个元素
            List<Object> processedList = new ArrayList<>();
            for (Object obj : (Collection<?>) result) {
                if (obj instanceof String || obj instanceof Number) {
                    // 简单类型直接添加
                    processedList.add(obj);
                } else {
                    // 对象类型进行字典回显处理
                    processedList.add(processObject(obj));
                }
            }
            return processedList;
        } else if (result instanceof String || result instanceof Number) {
            // 简单类型直接返回
            return result;
        } else {
            // 单个对象进行字典回显处理
            return processObject(result);
        }
    }

    /**
     * 处理单个对象
     * <p>
     * 遍历对象的所有字段，检查 @DictBind 注解和枚举类型字段，
     * 分别进行字典回显和枚举转换处理。
     *
     * @param obj 待处理的对象
     * @return 处理后的对象
     */
    private Object processObject(Object obj) {
        if (obj == null) {
            return null;
        }

        Class<?> sourceClass = obj.getClass();
        // 使用 MyBatis 的 MetaObject 进行属性访问
        MetaObject metaObject = SystemMetaObject.forObject(obj);
        // 获取类的所有字段（包括父类）
        List<Field> fields = getAllFields(sourceClass);

        for (Field field : fields) {
            // 处理 @DictBind 字典回显
            DictBind dictBind = field.getAnnotation(DictBind.class);
            if (dictBind != null) {
                fillDictText(dictBind, field, metaObject);
            }

            // 处理枚举类型字段（继承 BaseEnum 的枚举字段）
            if (BaseEnum.class.isAssignableFrom(field.getType()) && Enum.class.isAssignableFrom(field.getType())) {
                fillEnumValue(field, metaObject);
            }
        }

        return obj;
    }

    /**
     * 填充字典描述文本
     * <p>
     * 根据 @DictBind 注解的 code 查询字典表，
     * 获取对应的描述文本填充到 target 字段。
     *
     * @param dictBind   字典绑定注解
     * @param field      字段信息
     * @param metaObject MyBatis 元对象
     */
    private void fillDictText(DictBind dictBind, Field field, MetaObject metaObject) {
        String dictCode = dictBind.code();
        String targetField = dictBind.target();

        // 如果未指定 target 字段，默认使用 字段名 + "Text"
        if (targetField == null || targetField.isEmpty()) {
            targetField = field.getName() + "Text";
        }

        // 获取字段值
        Object value = metaObject.getValue(field.getName());
        if (value == null) {
            return;
        }

        // 查询字典标签
        String label = DictCache.getInstance().getDictLabel(dictCode, value.toString());
        if (label != null) {
            // 填充到 target 字段
            metaObject.setValue(targetField, label);
        }
    }

    /**
     * 填充枚举值
     * <p>
     * 将数据库存储的值转换为对应的枚举常量。
     * 根据字段类型从 DictCache 查询对应的枚举。
     *
     * @param field      字段信息
     * @param metaObject MyBatis 元对象
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseEnum<?>> void fillEnumValue(Field field, MetaObject metaObject) {
        Class<T> enumType = (Class<T>) field.getType();
        Object value = metaObject.getValue(field.getName());

        if (value == null) {
            return;
        }

        // 根据存储值查询枚举
        T enumConstant = DictCache.getInstance().getEnumByCode(enumType, value.toString());
        if (enumConstant != null) {
            // 将枚举设置回字段
            metaObject.setValue(field.getName(), enumConstant);
        }
    }

    /**
     * 获取类的所有字段（包括父类字段）
     * <p>
     * 递归遍历类的继承层级，获取所有声明的字段。
     *
     * @param clazz 类信息
     * @return 所有字段列表
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}