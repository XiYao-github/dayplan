package com.xiyao.dict.enums;


/**
 * 枚举基础接口
 * <p>
 * 所有业务枚举需实现此接口，提供统一的 code、name、desc 访问方式。
 * 用于实现数据库存储值与 Java 枚举的双向转换。
 *
 * <p>
 * <b>设计背景：</b>
 * 数据库中通常存储的是枚举的 code 值（如 1、2），
 * 而前端展示需要的是描述文本（如"正常"、"禁用"）。
 * 通过此接口统一管理枚举的这三种属性。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public enum DataStatus implements BaseEnum<Integer> {
 *     NORMAL(1, "正常"),
 *     DISABLED(2,  "禁用");
 *
 *     private final Integer code;
 *     private final String desc;
 *
 *     DataStatus(Integer code, String desc) {
 *         this.code = code;
 *         this.desc = desc;
 *     }
 *
 *     @Override
 *     public Integer getCode() { return code; }
 *
 *
 *     @Override
 *     public String getDesc() { return desc; }
 * }
 * }</pre>
 *
 * @param <T> code 值的类型，通常为 Integer 或 String
 * @author xiyao
 */
public interface BaseEnum<T> {

    /**
     * 获取存储到数据库的值
     * <p>
     * 即枚举的 code 值，用于持久化到数据库。
     * 例如：DataStatus.NORMAL.getCode() 返回 1
     *
     * @return 字典值或枚举存储值
     */
    T getCode();

    /**
     * 获取枚举描述
     * <p>
     * 返回枚举的中文描述，用于前端展示。
     * 例如：DataStatus.NORMAL.getDesc() 返回 "正常"
     *
     * @return 描述文本
     */
    String getDesc();
}