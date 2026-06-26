package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 每日记录 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class DailyRecordVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 记录类型(1.计划 2.记录 3.总结)
     */
    private Integer recordType;

    /**
     * 周期维度(1.日 2.周 3.月 4.年)
     */
    private Integer period;

    /**
     * 周期值
     */
    private String periodValue;

    /**
     * 记录内容
     */
    private String content;

    /**
     * 高光/成就
     */
    private String highlight;

    /**
     * 卡点/困难
     */
    private String blocker;

    /**
     * 分类(learn/work/life)
     */
    private String category;
}
