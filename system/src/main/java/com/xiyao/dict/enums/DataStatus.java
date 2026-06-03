package com.xiyao.dict.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据状态枚举
 * <p>
 * 用于表示业务数据的可用状态，与通用状态枚举 Status 区分使用。
 * DataStatus 用于描述数据本身的可用性，如数据的启用/暂停状态。
 *
 * <p>
 * <b>编码规范：</b>
 * <ul>
 *     <li>0：暂停（PAUSE）</li>
 *     <li>1：正常（NORMAL）</li>
 * </ul>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>字典数据状态：0-暂停使用、1-正常可用</li>
 *     <li>业务数据状态：数据是否对外提供服务</li>
 *     <li>与 Status 的区别：DataStatus 用于业务数据，Status 用于系统资源</li>
 * </ul>
 *
 * @author xiyao
 * @see Status
 * @see BaseEnum
 */
@Getter
@RequiredArgsConstructor
public enum DataStatus implements BaseEnum<Integer> {

    /**
     * 暂停状态
     * <p>
     * 数据暂停使用，但未被物理删除，保留历史记录
     */
    PAUSE(0, "暂停"),

    /**
     * 正常状态
     * <p>
     * 数据正常运行，对外提供服务
     */
    NORMAL(1, "正常");

    /**
     * 存储到数据库的值
     * <p>
     * 0-暂停，1-正常
     */
    private final Integer code;

    /**
     * 描述文本
     * <p>
     * 用于前端展示，如"正常"、"暂停"
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
     * 根据 JSON 中的值（如 0、1、"PAUSE"、"正常"）查找对应枚举。
     *
     * @param value JSON 中的值
     * @return 对应的 DataStatus 枚举实例
     */
    @JsonCreator
    public static DataStatus fromJson(Object value) {
        return BaseEnum.fromValue(DataStatus.class, value);
    }

}