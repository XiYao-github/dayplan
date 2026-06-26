package com.xiyao.service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 学习记录 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class StudyRecordVo {

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
     * 辅助图片列表
     */
    private List<String> imageUrls;

    /**
     * 备注
     */
    private String remark;
}
