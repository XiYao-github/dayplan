package com.xiyao.system.service;

import com.xiyao.system.vo.SysUserVo;

import java.util.List;

/**
 * 用户管理服务接口
 *
 * @author xiyao
 */
public interface ISysUserService {

    /**
     * 查询用户列表
     */
    List<SysUserVo> list(SysUserVo query);

    /**
     * 根据ID查询用户
     */
    SysUserVo getById(Long id);

    /**
     * 创建用户
     */
    boolean create(SysUserVo vo);

    /**
     * 更新用户
     */
    boolean update(SysUserVo vo);

    /**
     * 删除用户
     */
    boolean delete(Long id);

    /**
     * 分配角色
     */
    boolean assignRoles(Long userId, Long[] roleIds);

    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String password);

    /**
     * 修改状态
     */
    boolean updateStatus(Long id, Integer status);
}