package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 每日记录
 * </p>
 *
 * @author xiyao
 */
@Data
@TableName("daily_record")
@Accessors(chain = true)
public class DailyRecord {

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
     * 记录类型(1.计划 2.记录 3.总结)
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 周期维度(1.日 2.周 3.月 4.年)
     */
    @TableField("period")
    private Integer period;

    /**
     * 周期值
     */
    @TableField("period_value")
    private String periodValue;

    /**
     * 记录内容
     */
    @TableField("content")
    private String content;

    /**
     * 高光/成就
     */
    @TableField("highlight")
    private String highlight;

    /**
     * 卡点/困难
     */
    @TableField("blocker")
    private String blocker;

    /**
     * 分类(learn/work/life)
     */
    @TableField("category")
    private String category;

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
