package com.xiyao.dict.enums;

import cn.hutool.core.util.ObjectUtil;

/**
 * 枚举基础接口
 * <p>
 * 所有业务枚举类必须实现此接口，统一枚举的编码、描述和序列化方式。
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *     <li>code：存储到数据库的值，支持 Integer、String 等类型</li>
 *     <li>desc：描述文本，用于前端展示</li>
 *     <li>getValue()：Jackson 序列化时返回 code 值</li>
 *     <li>fromValue()：根据值反向查找枚举实例</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public enum Status implements BaseEnum<Integer> {
 *     DISABLED(0, "禁用"),
 *     NORMAL(1, "正常");
 *
 *     private final Integer code;
 *     private final String desc;
 * }
 * }</pre>
 *
 * @param <T> 枚举 code 的类型，支持 Integer、String 等
 * @author xiyao
 * @see Status
 * @see DataStatus
 */
public interface BaseEnum<T> {

    /**
     * 获取枚举的存储编码
     * <p>
     * 即存入数据库时的值，如 0、1、"A"、"B" 等。
     *
     * @return 枚举编码值
     */
    T getCode();

    /**
     * 获取枚举的描述文本
     * <p>
     * 用于前端展示，如"正常"、"禁用"、"暂停"等。
     *
     * @return 枚举描述
     */
    String getDesc();

    /**
     * 获取序列化值
     * <p>
     * Jackson 序列化时调用此方法，将枚举转换为 JSON。
     * 默认实现返回 getCode()，可覆盖自定义序列化逻辑。
     *
     * @return 序列化后的值
     */
    T getValue();

    /**
     * 根据值查找枚举实例
     * <p>
     * 支持通过 code、desc、name 三种方式匹配：
     * <ul>
     *     <li>code：精确匹配存储值</li>
     *     <li>desc：匹配描述文本</li>
     *     <li>name：匹配枚举常量名称</li>
     * </ul>
     *
     * @param enumClass 枚举类 Class
     * @param value      待匹配的值
     * @param <T>        枚举类型
     * @return 匹配的枚举实例，未找到返回 null
     */
    static <T extends BaseEnum<?>> T fromValue(Class<T> enumClass, Object value) {
        // 空值检查，避免后续处理空指针
        if (ObjectUtil.isNull(value)) {
            return null;
        }

        // 统一转为字符串进行匹配
        String strValue = String.valueOf(value);

        // 遍历枚举所有常量，尝试三种匹配方式
        for (T constant : enumClass.getEnumConstants()) {
            // 匹配方式一：精确匹配 code（存储值）
            // 使用 String.valueOf 统一类型后再比较，兼容 Integer 和 String 类型
            if (String.valueOf(constant.getCode()).equals(strValue)) {
                return constant;
            }
            // 匹配方式二：匹配 desc（描述文本）
            if (constant.getDesc().equals(strValue)) {
                return constant;
            }
            // 匹配方式三：匹配枚举名称（常量名）
            // 如 DISABLED、NORMAL 等，强制转换为 Enum 获取 name()
            if (((Enum<?>) constant).name().equals(strValue)) {
                return constant;
            }
        }
        // 三种方式都未匹配，返回 null
        return null;
    }
}