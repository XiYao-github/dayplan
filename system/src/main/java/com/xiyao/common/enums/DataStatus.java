package com.xiyao.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.xiyao.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public enum DataStatus implements BaseEnum<Integer> {

    /**
     * 暂停
     */
    PAUSE(0, "暂停"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    @EnumValue
    private final Integer code;

    private final String desc;

    @JsonCreator
    public static DataStatus fromValue(String value) {
        // 空值判断
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String input = value.trim();
        for (DataStatus status : values()) {
            // 匹配 code
            if (status.getCode().toString().equals(input)) {
                return status;
            }
            // 匹配 desc
            if (status.getDesc().equals(input)) {
                return status;
            }
            // 匹配枚举名
            if (status.name().equals(input)) {
                return status;
            }
        }
        // 都匹配不上，返回 null
        return null;
    }

    @JsonValue
    public Integer getValue() {
        return code;
    }
}
