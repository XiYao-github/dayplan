package com.xiyao.system.service;

import com.xiyao.system.vo.SysMenuVo;

import java.util.List;

/**
 * 菜单管理服务接口
 *
 * @author xiyao
 */
public interface ISysMenuService {

    /**
     * 查询菜单列表（树形）
     */
    List<SysMenuVo> listTree(SysMenuVo query);

    /**
     * 查询菜单列表（平铺）
     */
    List<SysMenuVo> list(SysMenuVo query);

    /**
     * 根据ID查询菜单
     */
    SysMenuVo getById(Long id);

    /**
     * 创建菜单
     */
    boolean create(SysMenuVo vo);

    /**
     * 更新菜单
     */
    boolean update(SysMenuVo vo);

    /**
     * 删除菜单
     */
    boolean delete(Long id);

    /**
     * 查询菜单下拉选项
     */
    List<SysMenuVo> listOptions();
}