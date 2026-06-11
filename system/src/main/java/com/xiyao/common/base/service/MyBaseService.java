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
import org.springframework.transaction.annotation.Transactional;

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
 *     <li>save(T entity)：插入数据</li>
 *     <li>saveBatch(list)：批量插入</li>
 *     <li>updateById(T entity)：根据 ID 更新</li>
 *     <li>removeById(id)：根据 ID 删除</li>
 *     <li>getById(id)：根据 ID 查询</li>
 *     <li>list(wrapper)：条件查询列表</li>
 *     <li>page(page, wrapper)：分页查询</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @author xiyao
 * @see IService
 */
public interface MyBaseService<T> extends IService<T> {

    /**
     * 批量插入实体对象集合
     *
     * @param entity 实体对象
     * @return 插入操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean insert(T entity) {
        return Db.save(entity);
    }

    /**
     * 批量根据ID更新实体对象集合
     *
     * @param entity 实体对象
     * @return 更新操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean update(T entity) {
        return Db.updateById(entity);
    }

    /**
     * 批量插入或更新实体对象集合
     *
     * @param entity 实体对象
     * @return 插入或更新操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean insertOrUpdate(T entity) {
        return Db.saveOrUpdate(entity);
    }

    /**
     * 批量插入实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 插入操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean insertBatch(Collection<T> entityList) {
        return Db.saveBatch(entityList);
    }

    /**
     * 批量根据ID更新实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 更新操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatch(Collection<T> entityList) {
        return Db.updateBatchById(entityList);
    }

    /**
     * 批量插入或更新实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 插入或更新操作是否成功的布尔值
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean insertOrUpdateBatch(Collection<T> entityList) {
        return Db.saveOrUpdateBatch(entityList);
    }


    default T queryByField(SFunction<T, ?> codeGetter, Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        return Db.lambdaQuery(this.getEntityClass()).eq(codeGetter, object).one();
    }

    default List<T> queryByField(SFunction<T, ?> codeGetter, Collection<?> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return Db.lambdaQuery(this.getEntityClass()).in(codeGetter, list).list();
    }

    default PageResult<T> queryPage(PageQuery query) {
        // 分页对象
        Page<T> build = query.build(this.getEntityClass());
        // 查询数据
        IPage<T> page = Db.page(build, this.getEntityClass());
        // 封装分页结果
        return PageResult.page(page, page.getRecords());
    }

    default PageResult<T> queryPage(PageQuery query, QueryWrapper<T> queryWrapper) {
        // 分页对象
        Page<T> build = query.build(this.getEntityClass());
        // 查询数据
        IPage<T> page = Db.page(build, queryWrapper);
        // 封装分页结果
        return PageResult.page(page, page.getRecords());
    }

    default <V> PageResult<V> queryPage(PageQuery query, QueryWrapper<T> queryWrapper, Class<V> entityClass) {
        // 分页对象
        Page<T> build = query.build(this.getEntityClass());
        // 查询数据
        IPage<T> page = Db.page(build, queryWrapper);
        // 封装分页结果
        return PageResult.page(page, ConvertUtils.sourceToTarget(page.getRecords(), entityClass));
    }
}