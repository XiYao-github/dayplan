package com.xiyao.common.base.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.utils.ConvertUtils;
import com.xiyao.common.utils.data.PageQuery;
import com.xiyao.common.utils.data.PageResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service 基类接口
 * <p>
 * 继承 MyBatis-Plus 的 IService，提供通用的 CRUD 操作和分页方法。
 * 所有业务 Service 接口应继承此接口。
 *
 * <p>
 * <b>提供的通用方法：</b>
 * <ul>
 *     <li>queryByField()：按字段查询单条/批量</li>
 *     <li>queryPage()：分页查询</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public interface UserService extends MyBaseService<User> {
 *     // 直接继承，无需实现通用方法
 * }
 *
 * public class UserServiceImpl extends MyBaseServiceImpl<UserMapper, User> implements UserService {
 *     // 直接使用父类提供的通用方法
 * }
 * }</pre>
 *
 * @param <T> 实体类型
 * @author xiyao
 * @see IService
 */
public interface MyBaseService<T> extends IService<T> {

    /**
     * 根据字段条件查询单个实体
     * <p>
     * 使用 lambdaQuery 查询匹配的第一条记录，返回第一条匹配的数据。
     * 注意：如果有多条匹配数据，也会返回其中一条，建议确保字段值唯一。
     *
     * @param codeGetter 字段 getter 方法引用，如 User::getUsername
     * @param object     字段值
     * @return 匹配的实体对象，未找到返回 null
     */
    default T queryByField(SFunction<T, ?> codeGetter, Object object) {
        // 空值校验，避免无效查询
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        // 构建 lambda 查询条件并执行查询
        return Db.lambdaQuery(this.getEntityClass()).eq(codeGetter, object).one();
    }

    /**
     * 根据字段条件批量查询实体列表
     * <p>
     * 使用 lambdaQuery 查询匹配的所有记录，返回列表。
     *
     * @param codeGetter 字段 getter 方法引用，如 User::getStatus
     * @param list       字段值集合（支持 IN 查询）
     * @return 匹配的实体列表，空集合表示未找到
     */
    default List<T> queryByField(SFunction<T, ?> codeGetter, Collection<?> list) {
        // 空集合校验
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 构建 lambda 查询条件并执行批量查询
        return Db.lambdaQuery(this.getEntityClass()).in(codeGetter, list).list();
    }

    /**
     * 分页查询（无条件）
     * <p>
     * 使用 PageQuery 构建分页参数，查询所有数据（受分页参数限制）。
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    default PageResult<T> queryPage(PageQuery query) {
        // 构建分页对象（包含分页参数和排序）
        Page<T> build = query.build(this.getEntityClass());
        // 执行分页查询
        IPage<T> page = Db.page(build, this.getEntityClass());
        // 封装分页结果并返回
        return PageResult.page(page, page.getRecords());
    }

    /**
     * 分页查询（带查询条件）
     * <p>
     * 使用 PageQuery 构建分页参数，QueryWrapper 构建查询条件。
     *
     * @param query        分页查询参数
     * @param queryWrapper 查询条件构造器
     * @return 分页结果
     */
    default PageResult<T> queryPage(PageQuery query, QueryWrapper<T> queryWrapper) {
        // 构建分页对象
        Page<T> build = query.build(this.getEntityClass());
        // 执行带条件的分页查询
        IPage<T> page = Db.page(build, queryWrapper);
        // 封装分页结果并返回
        return PageResult.page(page, page.getRecords());
    }

    /**
     * 分页查询（带查询条件并转换为 VO）
     * <p>
     * 使用 PageQuery 构建分页参数，QueryWrapper 构建查询条件，
     * 并将查询结果自动转换为 VO 类型。
     *
     * @param query        分页查询参数
     * @param queryWrapper 查询条件构造器
     * @param entityClass  目标 VO 类型，用于属性拷贝
     * @param <V>          VO 类型
     * @return 转换后的分页结果
     */
    default <V> PageResult<V> queryPage(PageQuery query, QueryWrapper<T> queryWrapper, Class<V> entityClass) {
        // 构建分页对象
        Page<T> build = query.build(this.getEntityClass());
        // 执行带条件的分页查询
        IPage<T> page = Db.page(build, queryWrapper);
        // 将实体列表转换为 VO 列表
        List<V> voList = ConvertUtils.sourceToTarget(page.getRecords(), entityClass);
        // 封装分页结果并返回
        return PageResult.page(page, voList);
    }
}