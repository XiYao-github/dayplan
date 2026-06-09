package com.xiyao.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期工具类
 *
 * <p>
 * 基于 Hutool 封装，提供常用的日期操作：
 * <ul>
 *     <li>格式化与解析</li>
 *     <li>日期时间计算</li>
 *     <li>当天开始/结束时间</li>
 *     <li>年龄计算</li>
 *     <li>日期范围判断</li>
 * </ul>
 *
 * @author xiyao
 */
public class DateUtils {

    /** 标准日期格式：yyyy-MM-dd */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 标准时间格式：HH:mm:ss */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 紧凑日期格式：yyyyMMdd */
    public static final String COMPACT_DATE_PATTERN = "yyyyMMdd";

    /** 紧凑时间格式：HHmmss */
    public static final String COMPACT_TIME_PATTERN = "HHmmss";

    /** 年月格式：yyyy-MM */
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    // ==================== 格式化与解析 ====================

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为指定格式
     *
     * @param dateTime 日期时间
     * @param pattern  格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期为 yyyy-MM-dd
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间为 HH:mm:ss
     *
     * @param time 时间
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateStr 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析任意格式的日期字符串
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDateTime
     */
    public static LocalDateTime parse(String dateStr, String pattern) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        Date date = DateUtil.parse(dateStr, pattern);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // ==================== 当天开始/结束时间 ====================

    /**
     * 获取当天的开始时间（00:00:00）
     *
     * @return 当天 0 点
     */
    public static LocalDateTime getStartOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     *
     * @param date 日期
     * @return 当天 0 点
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取当天的结束时间（23:59:59.999999999）
     *
     * @return 当天 23:59:59
     */
    public static LocalDateTime getEndOfDay() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    /**
     * 获取指定日期的结束时间
     *
     * @param date 日期
     * @return 当天 23:59:59
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    // ==================== 日期计算 ====================

    /**
     * 日期加减天数
     *
     * @param date   日期
     * @param days   天数（正数为加，负数为减）
     * @return 计算后的日期
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * 日期时间加减天数
     *
     * @param dateTime 日期时间
     * @param days     天数
     * @return 计算后的日期时间
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    /**
     * 日期加减月数
     *
     * @param date  日期
     * @param months 月数
     * @return 计算后的日期
     */
    public static LocalDate plusMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    /**
     * 日期加减年数
     *
     * @param date  日期
     * @param years 年数
     * @return 计算后的日期
     */
    public static LocalDate plusYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    /**
     * 计算两个日期之间的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时数
     *
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 小时数
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的分钟数
     *
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 分钟数
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    // ==================== 年龄计算 ====================

    /**
     * 根据出生日期计算年龄
     *
     * @param birthDate 出生日期
     * @return 年龄
     */
    public static int getAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 根据出生日期计算年龄（到指定日期）
     *
     * @param birthDate 出生日期
     * @param toDate    计算年龄的日期
     * @return 年龄
     */
    public static int getAge(LocalDate birthDate, LocalDate toDate) {
        return Period.between(birthDate, toDate).getYears();
    }

    // ==================== 日期范围判断 ====================

    /**
     * 判断日期是否在范围内
     *
     * @param date      日期
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 判断日期时间是否在范围内
     *
     * @param dateTime   日期时间
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    /**
     * 判断日期是否为今天
     *
     * @param date 日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return LocalDate.now().equals(date);
    }

    /**
     * 判断日期时间是否为今天
     *
     * @param dateTime 日期时间
     * @return 是否为今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return LocalDate.now().equals(dateTime.toLocalDate());
    }

    /**
     * 判断日期是否周末
     *
     * @param date 日期
     * @return 是否为周末
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 转换为 Date 对象
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 从 Date 转换
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private DateUtils() {
    }
}
