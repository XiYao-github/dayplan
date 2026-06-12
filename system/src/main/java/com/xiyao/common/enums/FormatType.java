package com.xiyao.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 日期格式与时间格式枚举
 * <p>
 * 提供常用的日期时间格式常量，支持格式字符串匹配。
 *
 * <p>
 * <b>日期格式符号说明：</b>
 * <ul>
 *     <li>"yyyy"：4位数年份，如2023年表示为"2023"</li>
 *     <li>"yy"：2位数年份，如2023年表示为"23"</li>
 *     <li>"MM"：2位数月份，01-12，如7月表示为"07"</li>
 *     <li>"M"：月份，1-12，如7月表示为"7"</li>
 *     <li>"dd"：2位数日期，01-31，如22日表示为"22"</li>
 *     <li>"d"：日期，1-31，如22日表示为"22"</li>
 *     <li>"EEEE"：星期全名，如星期三表示为"Wednesday"</li>
 *     <li>"E"：星期缩写，如星期三表示为"Wed"</li>
 *     <li>"HH"：24小时制小时，00-23，如下午5点表示为"17"</li>
 *     <li>"hh"：12小时制小时，01-12，如下午5点表示为"05"</li>
 *     <li>"mm"：分钟，00-59，如30分钟表示为"30"</li>
 *     <li>"ss"：秒数，00-59，如45秒表示为"45"</li>
 *     <li>"SSS"：毫秒，000-999，如123毫秒表示为"123"</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum FormatType {

    /**
     * 例如：2023年表示为"23"
     */
    YY("yy"),

    /**
     * 例如：2023年表示为"2023"
     */
    YYYY("yyyy"),

    /**
     * 例如，2023年7月可以表示为 "2023-07"
     */
    YYYY_MM("yyyy-MM"),

    /**
     * 例如，日期 "2023年7月22日" 可以表示为 "2023-07-22"
     */
    YYYY_MM_DD("yyyy-MM-dd"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分"，则可以表示为 "2023-07-22 15:30"
     */
    YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023-07-22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),

    /**
     * 例如：下午3点30分45秒，表示为 "15:30:45"
     */
    HH_MM_SS("HH:mm:ss"),

    /**
     * 例如，2023年7月可以表示为 "2023/07"
     */
    YYYY_MM_SLASH("yyyy/MM"),

    /**
     * 例如，日期 "2023年7月22日" 可以表示为 "2023/07/22"
     */
    YYYY_MM_DD_SLASH("yyyy/MM/dd"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023/07/22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SLASH("yyyy/MM/dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023/07/22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_SLASH("yyyy/MM/dd HH:mm:ss"),

    /**
     * 例如，2023年7月可以表示为 "2023.07"
     */
    YYYY_MM_DOT("yyyy.MM"),

    /**
     * 例如，日期 "2023年7月22日" 可以表示为 "2023.07.22"
     */
    YYYY_MM_DD_DOT("yyyy.MM.dd"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分"，则可以表示为 "2023.07.22 15:30"
     */
    YYYY_MM_DD_HH_MM_DOT("yyyy.MM.dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023.07.22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_DOT("yyyy.MM.dd HH:mm:ss"),

    /**
     * 例如，2023年7月可以表示为 "202307"
     */
    YYYYMM("yyyyMM"),

    /**
     * 例如，2023年7月22日可以表示为 "20230722"
     */
    YYYYMMDD("yyyyMMdd"),

    /**
     * 例如，2023年7月22日下午3点可以表示为 "2023072215"
     */
    YYYYMMDDHH("yyyyMMddHH"),

    /**
     * 例如，2023年7月22日下午3点30分可以表示为 "202307221530"
     */
    YYYYMMDDHHMM("yyyyMMddHHmm"),

    /**
     * 例如，2023年7月22日下午3点30分45秒可以表示为 "20230722153045"
     */
    YYYYMMDDHHMMSS("yyyyMMddHHmmss");

    /**
     * 时间格式
     */
    private final String format;

    /**
     * 根据字符串获取对应的日期格式枚举
     * <p>
     * 遍历所有格式枚举，查找包含给定字符串的格式。
     *
     * @param str 日期格式字符串
     * @return 匹配的 FormatType，找不到返回 null
     */
    public static FormatType getFormatsType(String str) {
        for (FormatType value : values()) {
            if (StrUtil.contains(str, value.getFormat())) {
                return value;
            }
        }
        return null;
    }
}
