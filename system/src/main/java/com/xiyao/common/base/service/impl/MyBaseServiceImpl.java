package com.xiyao.common.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.common.base.mapper.MyBaseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service 实现基类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供通用的 CRUD 实现。
 * 所有业务 ServiceImpl 应继承此类。
 *
 * <p>
 * <b>泛型说明：</b>
 * <ul>
 *     <li>M：继承自 MyBaseMapper 的 Mapper 接口</li>
 *     <li>T：实体类型</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Service
 * public class UserServiceImpl extends MyBaseServiceImpl<UserMapper, User> implements UserService {
 *
 *     // 无需重写通用 CRUD 方法，直接使用父类提供的方法
 *     public User getAdminUser() {
 *         return getOne(new LambdaQueryWrapper<User>().eq(User::getAdminType, 1));
 *     }
 * }
 * }</pre>
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @author xiyao
 * @see ServiceImpl
 */
public abstract class MyBaseServiceImpl<M extends MyBaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 生成业务编号
     * <p>
     * 根据指定的时间字段统计当日数据条数，生成格式为"前缀+年月日+序号"的编号。
     * <p>
     * 编号格式：prefix + yyMMdd + 4位序号（如 TX202606100001）
     * <p>
     * <b>使用场景：</b>订单号、流水号等需要每日连续的编号
     *
     * @param prefix     编号前缀，用于标识业务类型（如"TX"表示提现，"DD"表示订单）
     * @param codeGetter 实体类中日期字段的 Getter 方法引用，用于确定统计哪一天的记录
     * @return 生成的业务编号，如 "TX202606100001"
     */
    public String getNumber(String prefix, SFunction<T, LocalDateTime> codeGetter) {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 计算当天的时间范围：当天零点 到明天零点（不包含明天）
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // 构建查询条件：统计当天该字段在时间范围内的记录数
        LambdaQueryWrapper<T> lqw = new LambdaQueryWrapper<>();
        lqw.ge(codeGetter, start);  // >= 当天零点
        lqw.lt(codeGetter, end);   // < 明天零点

        // 获取实体类对应的表名
        // TableInfoHelper 会根据泛型 T 自动解析实体类上的 @TableName 注解获取表名
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        String tableName = tableInfo.getTableName();

        // 执行统计查询，统计当天该前缀的记录数
        // 注意：queryCount 是自定义方法，需要在 MyBaseMapper 中实现
        // TODO: 后续考虑redis缓存业务前缀订单数量，避免重复查询
        Integer count = this.baseMapper.queryCount(tableName, lqw);
        // 如果查询结果为 null（表中无数据），从 0 开始计数
        // 使用 ObjectUtil.defaultIfNull 处理 count 为 null 的情况
        int currentCount = ObjectUtil.defaultIfNull(count, 0);

        // 生成编号：前缀 + 年月日（yyMMdd格式）+ 4位序号（从1开始）
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String numberStr = String.format("%04d", currentCount + 1);

        return prefix + dateStr + numberStr;
    }

}