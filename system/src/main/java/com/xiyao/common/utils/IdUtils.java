package com.xiyao.dict.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * ID生成工具类
 *
 * <p>
 * 提供多种ID生成方式：
 * <ul>
 *     <li>雪花算法ID（分布式场景）</li>
 *     <li>UUID（无序、全球唯一）</li>
 *     <li>毫秒级时间戳ID</li>
 *     <li>数据库当天数据统计</li>
 * </ul>
 *
 * @author xiyao
 */
public class IdUtils {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 获取雪花算法ID（分布式唯一）
     * <p>
     * 适用场景：订单号、流水号等需要全局唯一的ID
     *
     * @return 19位长整数ID
     */
    public static Long getSnowflakeId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 获取雪花算法ID（String类型）
     *
     * @return 字符串形式的ID
     */
    public static String getSnowflakeIdStr() {
        return String.valueOf(SNOWFLAKE.nextId());
    }

    /**
     * 获取UUID（无序）
     * <p>
     * 适用场景：激活码、临时文件等
     *
     * @return 32位无中划线UUID
     */
    public static String getUuid() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 获取带中划线的UUID
     *
     * @return 标准的36位UUID
     */
    public static String getUuidWithDash() {
        return IdUtil.randomUUID();
    }

    /**
     * 获取毫秒级时间戳（13位）
     *
     * @return 当前毫秒时间戳
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取秒级时间戳（10位）
     *
     * @return 当前秒时间戳
     */
    public static long getSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 生成业务编码
     * <p>
     * 格式：前缀 + 年月日时分秒 + 4位随机数
     *
     * @param prefix 前缀（如：ORDER、USER）
     * @return 业务编码，如 ORDER202606091430521234
     */
    public static String getBusinessCode(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
        return prefix + date + random;
    }

    /**
     * 查询指定实体类当天数据总数
     *
     * @param entityClass 实体类类型
     * @param timeField   时间字段名（如 "createTime"、"time"）
     * @return 当天记录数
     */
    public static <T> long getTodayCount(Class<T> entityClass, String timeField) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return Db.count(
                new QueryWrapper<T>()
                        .between(timeField, startOfDay, endOfDay)
        );
    }

    /**
     * 根据时间和实体类型生成业务ID
     * <p>
     * 格式：时间戳(8位) + 当天该类型数据的 count(6位)
     *
     * @param entityClass 实体类类型
     * @param timeField   时间字段名（如 "createTime"、"time"）
     * @return 基于时间和当天count的业务ID
     */
    public static <T> Long generateBusinessId(Class<T> entityClass, String timeField) {
        return generateBusinessId(entityClass, timeField, LocalDateTime.now());
    }

    /**
     * 根据时间和实体类型生成业务ID
     * <p>
     * 格式：时间戳(8位) + 当天该类型数据的 count(6位)
     *
     * @param entityClass 实体类类型
     * @param timeField   时间字段名（如 "createTime"、"time"）
     * @param time        参考时间
     * @return 基于时间和当天count的业务ID
     */
    public static <T> Long generateBusinessId(Class<T> entityClass, String timeField, LocalDateTime time) {
        LocalDateTime startOfDay = time.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = time.toLocalDate().atTime(LocalTime.MAX);

        long count = Db.count(
                new QueryWrapper<T>()
                        .between(timeField, startOfDay, endOfDay)
        );

        String dateStr = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String countStr = String.format("%06d", count + 1);

        return Long.parseLong(dateStr + countStr);
    }

    private IdUtils() {
    }
}
