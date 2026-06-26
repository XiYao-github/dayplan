package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 写作请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class WritingDto {

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
     * 辅助图片(多个用逗号分隔)
     */
    private String imageUrls;

    /**
     * 备注
     */
    private String remark;
}
