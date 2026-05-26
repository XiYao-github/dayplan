package com.xiyao.system.service;

import com.xiyao.system.entity.vo.SysRoleVo;

import java.util.List;

/**
 * 角色管理服务接口
 *
 * @author xiyao
 */
public interface ISysRoleService {

    /**
     * 查询角色列表
     */
    List<SysRoleVo> list(SysRoleVo query);

    /**
     * 根据ID查询角色
     */
    SysRoleVo getById(Long id);

    /**
     * 创建角色
     */
    boolean create(SysRoleVo vo);

    /**
     * 更新角色
     */
    boolean update(SysRoleVo vo);

    /**
     * 删除角色
     */
    boolean delete(Long id);

    /**
     * 查询角色已分配菜单
     */
    Long[] getMenuIds(Long roleId);

    /**
     * 分配菜单
     */
    boolean assignMenus(Long roleId, Long[] menuIds);
}