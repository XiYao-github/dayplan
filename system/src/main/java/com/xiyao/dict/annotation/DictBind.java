package com.xiyao.dict.annotation;

import java.lang.annotation.*;

/**
 * 字段数据绑定注解
 * <p>
 * 标注在实体字段上，标记该字段为字典值字段。
 * MyBatis 结果集拦截器（DictInterceptor）会自动查询字典缓存，
 * 将对应的描述文本（label）填充到指定的 target 字段。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>数据库存储的是 dictValue（如 "1"）</li>
 *     <li>前端需要展示的是 dictLabel（如 "正常"）</li>
 *     <li>通过 @DictBind 注解自动完成值到标签的转换</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class UserVO {
 *     // status 字段存储的是 0/1，statusText 字段自动填充为"禁用"/"正常"
 *     @DictBind(code = "status", target = "statusText")
 *     private Integer status;
 *     private String statusText;
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.dict.interceptor.DictInterceptor
 */
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictBind {

    /**
     * 字典类型编码
     * <p>
     * 用于从字典缓存中查询对应字典项，对应 sys_dict_data 表的 dict_code 字段。
     * 例如：code = "status" 表示查询字典类型为 status 的字典数据
     *
     * @return 字典编码（对应 dict_code）
     */
    String code();

    /**
     * 字典描述回显目标字段
     * <p>
     * 字典标签（dictLabel）将被填充到此字段。
     * 必须显式指定，不允许为空。
     * <p>
     * 例如：status 字段的 label 应填充到 statusText 字段
     *
     * @return 目标字段名（用于接收字典标签）
     */
    String target();
}