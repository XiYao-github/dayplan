package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 写作统计 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class WritingStatsVo {

    /**
     * 连续打卡天数
     */
    private Integer continuousDays;

    /**
     * 累计打卡天数
     */
    private Integer totalDays;
}
