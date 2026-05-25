package com.xiyao.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.common.base.mapper.MyBaseMapper;

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
 * public class UserServiceImpl extends MyBaseServiceImpl&lt;UserMapper, User&gt; implements UserService {
 *
 *     // 无需重写通用 CRUD 方法，直接使用父类提供的方法
 *     public User getAdminUser() {
 *         return getOne(new LambdaQueryWrapper&lt;User&gt;().eq(User::getAdminType, 1));
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

}