package com.xiyao.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * ID 生成工具类
 * <p>
 * 提供多种 ID 生成方式：
 * <ul>
 *     <li>雪花算法 ID（分布式场景，全局唯一）</li>
 *     <li>UUID（无序，全球唯一）</li>
 *     <li>毫秒级时间戳 ID</li>
 *     <li>业务编码（时间戳+随机数）</li>
 *     <li>数据库当天数据统计</li>
 * </ul>
 *
 * @author xiyao
 */
public class IdUtils {

    /**
     * 雪花算法生成器
     * <p>
     * 使用 Hutool 的雪花算法，默认 workerId=1, dataCenterId=1
     * 可生成 19 位不重复的长整型 ID
     */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 获取雪花算法 ID（分布式唯一）
     * <p>
     * 适用场景：订单号、流水号等需要全局唯一的 ID
     *
     * @return 19 位长整数 ID
     */
    public static Long getSnowflakeId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 获取雪花算法 ID（String 类型）
     * <p>
     * 将长整型 ID 转换为字符串，适用于需要字符串类型的场景。
     *
     * @return 字符串形式的 ID
     */
    public static String getSnowflakeIdStr() {
        return String.valueOf(SNOWFLAKE.nextId());
    }

    /**
     * 获取 UUID（无序）
     * <p>
     * 适用场景：激活码、临时文件、Session ID 等
     *
     * @return 32 位无中划线 UUID
     */
    public static String getUuid() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 获取带中划线的 UUID
     * <p>
     * 返回标准格式的 UUID，包含中划线。
     *
     * @return 36 位标准格式 UUID
     */
    public static String getUuidWithDash() {
        return IdUtil.randomUUID();
    }

    /**
     * 获取毫秒级时间戳（13 位）
     * <p>
     * 返回当前时间的毫秒值。
     *
     * @return 当前毫秒时间戳
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取秒级时间戳（10 位）
     * <p>
     * 返回当前时间的秒值。
     *
     * @return 当前秒时间戳
     */
    public static long getSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 生成业务编码
     * <p>
     * 格式：前缀 + 年月日时分秒 + 4 位随机数
     * <p>
     * 适用场景：需要具有一定随机性的业务编号
     *
     * @param prefix 前缀（如：ORDER、USER）
     * @return 业务编码，如 ORDER202606091430521234
     */
    public static String getBusinessCode(String prefix) {
        // 获取当前时间并格式化为字符串
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 生成 4 位随机大写字母
        String random = IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
        // 拼接前缀、时间戳、随机数
        return prefix + date + random;
    }

    /**
     * 查询指定实体类当天数据总数
     * <p>
     * 统计当天指定时间字段的记录数。
     *
     * @param entityClass 实体类类型
     * @param timeField 时间字段名（如 "createTime"、"time"）
     * @return 当天记录数
     */
    public static <T> long getTodayCount(Class<T> entityClass, String timeField) {
        // 计算当天的时间范围
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 构建查询条件：时间在当天范围内
        return Db.count(
                new QueryWrapper<T>()
                        .between(timeField, startOfDay, endOfDay)
        );
    }

    /**
     * 根据时间和实体类型生成业务 ID
     * <p>
     * 格式：时间戳（8 位年月日）+ 当天该类型数据的 count（6 位）
     * <p>
     * 适用场景：需要按日期分组且连续的编号
     *
     * @param entityClass 实体类类型
     * @param timeField   时间字段名（如 "createTime"、"time"）
     * @return 基于时间和当天 count 的业务 ID
     */
    public static <T> Long generateBusinessId(Class<T> entityClass, String timeField) {
        return generateBusinessId(entityClass, timeField, LocalDateTime.now());
    }

    /**
     * 根据时间和实体类型生成业务 ID（指定参考时间）
     * <p>
     * 格式：时间戳（8 位年月日）+ 当天该类型数据的 count（6 位）
     *
     * @param entityClass 实体类类型
     * @param timeField   时间字段名（如 "createTime"、"time"）
     * @param time        参考时间
     * @return 基于时间和当天 count 的业务 ID
     */
    public static <T> Long generateBusinessId(Class<T> entityClass, String timeField, LocalDateTime time) {
        // 计算参考时间当天的范围
        LocalDateTime startOfDay = time.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = time.toLocalDate().atTime(LocalTime.MAX);

        // 统计当天记录数
        long count = Db.count(
                new QueryWrapper<T>()
                        .between(timeField, startOfDay, endOfDay)
        );

        // 生成日期字符串（如 20260611）
        String dateStr = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 生成序号字符串，6 位不足补0（如 000001）
        String countStr = String.format("%06d", count + 1);

        // 拼接并转换为长整型
        return Long.parseLong(dateStr + countStr);
    }
}