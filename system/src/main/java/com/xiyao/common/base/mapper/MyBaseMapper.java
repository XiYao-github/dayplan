package com.xiyao.common.base.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
 * <p>
 * <b>扩展方法：</b>
 * <ul>
 *     <li>queryCount(table, wrapper)：自定义条件统计</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @author xiyao
 * @see BaseMapper
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {

    /**
     * 自定义条件获取数据条数
     * <p>
     * 使用 @InterceptorIgnore 忽略 MyBatis-Plus 的逻辑删除过滤，
     * 直接执行 COUNT 查询。
     * <p>
     * <b>使用场景：</b>
     * <ul>
     *     <li>统计当日数据量（如 getNumber 方法中统计当日记录数）</li>
     *     <li>需要绕过逻辑删除的统计场景</li>
     * </ul>
     *
     * @param table   表名（动态传入）
     * @param wrapper 查询条件构造器
     * @return 符合条件的数据条数
     */
    @InterceptorIgnore(illegalSql = "1")
    @Select("select count(1) from ${table} ${ew.customSqlSegment}")
    Integer queryCount(@Param("table") String table, @Param(Constants.WRAPPER) Wrapper<T> wrapper);
}