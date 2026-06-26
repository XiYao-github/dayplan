package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class StudyDto {

    /**
     * 学习主题
     */
    private String subject;

    /**
     * 具体知识点
     */
    private String topic;

    /**
     * 学习时长(分钟)
     */
    private Integer duration;

    /**
     * 辅助图片(多个用逗号分隔)
     */
    private String imageUrls;

    /**
     * 备注
     */
    private String remark;
}
