package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <p>
 * 打卡记录
 * </p>
 *
 * @author xiyao
 */
@Data
@TableName("checkin_record")
@Accessors(chain = true)
public class CheckinRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 打卡类型(1.睡觉 2.健身)
     */
    @TableField("checkin_type")
    private Integer checkinType;

    /**
     * 打卡日期
     */
    @TableField("record_date")
    private LocalDate recordDate;

    /**
     * 上床时间（睡觉打卡）
     */
    @TableField("bed_time")
    private LocalTime bedTime;

    /**
     * 开始时间（健身打卡）
     */
    @TableField("start_time")
    private LocalTime startTime;

    /**
     * 结束时间（健身打卡）
     */
    @TableField("end_time")
    private LocalTime endTime;

    /**
     * 时长(分钟)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 运动类型(strength/cardio/stretch)
     */
    @TableField("exercise_type")
    private String exerciseType;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
