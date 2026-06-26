package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * <p>
 * 学习记录
 * </p>
 *
 * @author xiyao
 */
@Data
@TableName("study_record")
@Accessors(chain = true)
public class StudyRecord {

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
     * 记录日期
     */
    @TableField("record_date")
    private LocalDate recordDate;

    /**
     * 学习主题
     */
    @TableField("subject")
    private String subject;

    /**
     * 具体知识点
     */
    @TableField("topic")
    private String topic;

    /**
     * 学习时长(分钟)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 辅助图片(多个用逗号分隔)
     */
    @TableField("image_urls")
    private String imageUrls;

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
