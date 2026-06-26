package com.xiyao.service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 写作记录 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class WritingRecordVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    /**
     * 标题/简介
     */
    private String title;

    /**
     * 背单词数量
     */
    private Integer wordCount;

    /**
     * 原文
     */
    private String sourceText;

    /**
     * 译文
     */
    private String translatedText;

    /**
     * 辅助图片列表
     */
    private List<String> imageUrls;

    /**
     * 备注
     */
    private String remark;
}
