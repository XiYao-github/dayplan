package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习统计 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class StudyStatsVo {

    /**
     * 连续打卡天数
     */
    private Integer continuousDays;

    /**
     * 本周学习总时长（分钟）
     */
    private Integer thisWeekDuration;

    /**
     * 累计打卡天数
     */
    private Integer totalDays;
}
