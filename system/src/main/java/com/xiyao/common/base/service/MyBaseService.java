package com.xiyao.common.base.service;

import com.baomidou.mybatisplus.extension.service.IService;

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

}