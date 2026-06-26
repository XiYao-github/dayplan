package com.xiyao.service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <p>
 * 打卡记录 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class CheckinRecordVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 打卡类型(1.睡觉 2.健身)
     */
    private Integer checkinType;

    /**
     * 打卡日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    /**
     * 上床时间（睡觉打卡）
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime bedTime;

    /**
     * 开始时间（健身打卡）
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /**
     * 结束时间（健身打卡）
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /**
     * 时长(分钟)
     */
    private Integer duration;

    /**
     * 运动类型(strength/cardio/stretch)
     */
    private String exerciseType;

    /**
     * 备注
     */
    private String remark;
}
