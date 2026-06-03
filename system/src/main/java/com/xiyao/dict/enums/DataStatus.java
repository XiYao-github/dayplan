package com.xiyao.dict.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataStatus implements BaseEnum<Integer> {

    /**
     * 暂停
     */
    PAUSE(0, "暂停"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    /**
     * 存储到数据库的值
     */
    private final Integer code;

    /**
     * 描述文本
     * <p>
     * 用于前端展示，如"正常"、"暂停"
     */
    private final String desc;

    // 序列化
    @Override
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    // 反序列化
    @JsonCreator
    public static DataStatus fromJson(Object value) {
        return BaseEnum.fromValue(DataStatus.class, value);
    }

}