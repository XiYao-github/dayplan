package com.xiyao.common.base.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.common.base.mapper.MyBaseMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 分页对象转换为 VO 分页
     *
     * @param page      实体分页
     * @param voClass   VO 类型
     * @param converter 转换函数
     * @param <R>       VO 类型
     * @return VO 分页
     */
    protected <R> Page<R> convertToVoPage(Page<T> page, Class<R> voClass, Function<T, R> converter) {
        List<R> records = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());

        Page<R> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }
}
