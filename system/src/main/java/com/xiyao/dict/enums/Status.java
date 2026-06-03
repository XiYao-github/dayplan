package com.xiyao.dict.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用状态枚举
 * <p>
 * 用于表示系统资源的启用/禁用状态，如用户账户状态、功能开关等。
 *
 * <p>
 * <b>编码规范：</b>
 * <ul>
 *     <li>0：禁用（DISABLED）</li>
 *     <li>1：正常（NORMAL）</li>
 * </ul>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>用户账户状态：0-禁用（无法登录）、1-正常</li>
 *     <li>功能开关：0-关闭、1-开启</li>
 *     <li>数据状态：与 DataStatus 区分，此枚举用于业务无关的通用状态</li>
 * </ul>
 *
 * @author xiyao
 * @see DataStatus
 * @see BaseEnum
 */
@Getter
@RequiredArgsConstructor
public enum Status implements BaseEnum<Integer> {

    /**
     * 禁用状态
     * <p>
     * 表示资源处于不可用状态，如账户被禁用、功能被关闭
     */
    DISABLED(0, "禁用"),

    /**
     * 正常状态
     * <p>
     * 表示资源处于可用状态
     */
    NORMAL(1, "正常");

    /**
     * 存储到数据库的值
     * <p>
     * 0-禁用，1-正常
     */
    private final Integer code;

    /**
     * 描述文本
     * <p>
     * 用于前端展示，如"禁用"、"正常"
     */
    private final String desc;

    /**
     * 序列化方法
     * <p>
     * Jackson 将枚举序列化为 JSON 时调用此方法，返回 code 值。
     * 例如：NORMAL 序列化为 1
     *
     * @return 序列化后的值（code）
     */
    @Override
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    /**
     * 反序列化方法
     * <p>
     * Jackson 将 JSON 反序列化为枚举时调用此方法。
     * 根据 JSON 中的值（如 0、1、"DISABLED"、"正常"）查找对应枚举。
     *
     * @param value JSON 中的值
     * @return 对应的 Status 枚举实例
     */
    @JsonCreator
    public static Status fromJson(Object value) {
        return BaseEnum.fromValue(Status.class, value);
    }
}