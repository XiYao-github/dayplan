package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 每日统计 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class DailyStatsVo {

    /**
     * 今日状态
     */
    private PeriodStatus today;

    /**
     * 本周状态
     */
    private PeriodStatus thisWeek;

    /**
     * 本月状态
     */
    private PeriodStatus thisMonth;

    @Data
    @Accessors(chain = true)
    public static class PeriodStatus {
        /**
         * 计划（当日为布尔，本周/月为数字）
         */
        private Object plan;

        /**
         * 记录（当日为布尔，本周/月为数字）
         */
        private Object record;

        /**
         * 总结（当日为布尔，本周/月为数字）
         */
        private Object summary;
    }
}
