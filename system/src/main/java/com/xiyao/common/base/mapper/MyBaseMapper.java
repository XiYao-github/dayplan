package com.xiyao.common.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Mapper 基类接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供通用的 CRUD 操作。
 * 所有业务 Mapper 应继承此接口。
 *
 * <p>
 * <b>提供的通用方法：</b>
 * <ul>
 *     <li>insert(T entity)：插入数据</li>
 *     <li>update(T entity)：根据 ID 更新数据</li>
 *     <li>deleteById(Serializable id)：根据 ID 删除</li>
 *     <li>selectById(Serializable id)：根据 ID 查询</li>
 *     <li>selectList(wrapper)：条件查询列表</li>
 *     <li>selectPage(page, wrapper)：分页查询</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @author xiyao
 * @see BaseMapper
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {

    /**
     * 自定义条件获取数据条数
     *
     * @param table   表名
     * @param wrapper 条件
     * @return count
     */
    // @InterceptorIgnore(illegalSql = "1")
    // @Select("select count(1) from ${table} ${ew.customSqlSegment}")
    // Integer queryCount(@Param("table") String table, @Param(Constants.WRAPPER) Wrapper<T> wrapper);
}