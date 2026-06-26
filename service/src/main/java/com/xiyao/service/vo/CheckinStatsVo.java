package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 打卡统计 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class CheckinStatsVo {

    /**
     * 连续打卡天数
     */
    private Integer continuousDays;

    /**
     * 累计打卡天数
     */
    private Integer totalDays;

    /**
     * 本周打卡次数
     */
    private Integer thisWeek;

    /**
     * 本月打卡次数
     */
    private Integer thisMonth;

    /**
     * 最近7天打卡状态
     */
    private List<DayStatus> last7Days;

    @Data
    @Accessors(chain = true)
    public static class DayStatus {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 是否已打卡
         */
        private Boolean done;

        /**
         * 上床时间（睡觉打卡特有）
         */
        private String bedTime;

        /**
         * 运动类型（健身打卡特有）
         */
        private String exerciseType;
    }
}
