package com.xiyao.dict.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status implements BaseEnum<Integer> {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    private final Integer code;

    private final String desc;

    // 序列化
    @Override
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    // 反序列化
    @JsonCreator
    public static Status fromJson(Object value) {
        return BaseEnum.fromValue(Status.class, value);
    }
}