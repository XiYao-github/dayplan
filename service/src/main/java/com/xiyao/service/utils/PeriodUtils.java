package com.xiyao.service.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * <p>
 * 周期工具类
 * </p>
 *
 * @author xiyao
 */
public class PeriodUtils {

    /**
     * 周期维度：日
     */
    public static final int PERIOD_DAY = 1;

    /**
     * 周期维度：周
     */
    public static final int PERIOD_WEEK = 2;

    /**
     * 周期维度：月
     */
    public static final int PERIOD_MONTH = 3;

    /**
     * 周期维度：年
     */
    public static final int PERIOD_YEAR = 4;

    private PeriodUtils() {
    }

    /**
     * 获取当前日 period_value
     *
     * @return 如 "2026-06-25"
     */
    public static String today() {
        return LocalDate.now().toString();
    }

    /**
     * 获取当前周 period_value
     *
     * @return 如 "2026-W26"
     */
    public static String thisWeek() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekOfYear = LocalDate.now().get(weekFields.weekOfYear());
        return LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);
    }

    /**
     * 获取当前月 period_value
     *
     * @return 如 "2026-06"
     */
    public static String thisMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 获取当前年 period_value
     *
     * @return 如 "2026"
     */
    public static String thisYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    /**
     * 根据周期维度获取当前 period_value
     *
     * @param period 周期维度
     * @return period_value
     */
    public static String getCurrentPeriodValue(int period) {
        return switch (period) {
            case PERIOD_DAY -> today();
            case PERIOD_WEEK -> thisWeek();
            case PERIOD_MONTH -> thisMonth();
            case PERIOD_YEAR -> thisYear();
            default -> today();
        };
    }
}
