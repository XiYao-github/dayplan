package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * <p>
 * 写作记录
 * </p>
 *
 * @author xiyao
 */
@Data
@TableName("writing_record")
@Accessors(chain = true)
public class WritingRecord {

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
     * 标题/简介
     */
    @TableField("title")
    private String title;

    /**
     * 背单词数量
     */
    @TableField("word_count")
    private Integer wordCount;

    /**
     * 原文
     */
    @TableField("source_text")
    private String sourceText;

    /**
     * 译文
     */
    @TableField("translated_text")
    private String translatedText;

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
