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
     *
     * @param prefix     编号前缀，用于标识业务类型
     * @param codeGetter 实体类中日期字段的 Getter 方法引用
     * @return 生成的业务编号
     */
    public String getNumber(String prefix, SFunction<T, LocalDateTime> codeGetter) {
        LocalDate today = LocalDate.now(); // 当前时间
        LocalDateTime start = today.atStartOfDay(); // 当天零点
        LocalDateTime end = today.plusDays(1).atStartOfDay(); // 明天零点

        LambdaQueryWrapper<T> lqw = new LambdaQueryWrapper<>();
        lqw.ge(codeGetter, start).lt(codeGetter, end);

        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        String tableName = tableInfo.getTableName();

        Integer count = this.baseMapper.queryCount(tableName, lqw);
        int currentCount = ObjectUtil.defaultIfNull(count,0) ;

        return prefix + today.format(DateTimeFormatter.ofPattern("yyMMdd")) + String.format("%04d", currentCount + 1);
    }

}
