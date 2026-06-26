package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * <p>
 * 健身打卡请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class WorkoutCheckinDto {

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    LocalTime endTime;

    /**
     * 运动类型(strength/cardio/stretch)
     */
    private String exerciseType;

    /**
     * 备注
     */
    private String remark;
}
