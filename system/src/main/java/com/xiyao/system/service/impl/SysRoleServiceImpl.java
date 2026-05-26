package com.xiyao.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.service.ISysRoleService;
import com.xiyao.system.vo.SysRoleVo;
import com.xiyao.system.entity.SysRole;
import com.xiyao.system.entity.SysRoleMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 *
 * @author xiyao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements ISysRoleService {

    @Override
    public List<SysRoleVo> list(SysRoleVo query) {
        List<SysRole> roles = Db.lambdaQuery(SysRole.class)
                .eq(ObjectUtil.isNotNull(query.getId()), SysRole::getId, query.getId())
                .like(ObjectUtil.isNotNull(query.getName()), SysRole::getName, query.getName())
                .eq(ObjectUtil.isNotNull(query.getType()), SysRole::getType, query.getType())
                .eq(ObjectUtil.isNotNull(query.getStatus()), SysRole::getStatus, query.getStatus())
                .list();

        return roles.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public SysRoleVo getById(Long id) {
        SysRole role = Db.lambdaQuery(SysRole.class)
                .eq(SysRole::getId, id)
                .one();
        if (role == null) {
            return null;
        }
        return convertToVo(role);
    }

    @Override
    public boolean create(SysRoleVo vo) {
        // 检查角色名唯一性
        Long count = Db.lambdaQuery(SysRole.class)
                .eq(SysRole::getName, vo.getName())
                .count();
        if (count > 0) {
            throw new RuntimeException("角色名称已存在");
        }

        SysRole role = new SysRole();
        role.setName(vo.getName());
        role.setType(vo.getType());
        role.setSort(vo.getSort() != null ? vo.getSort() : 0);
        role.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        role.setRemark(vo.getRemark());
        role.setDeleted(0);
        Db.save(role);

        // 分配菜单
        if (vo.getMenuIds() != null && vo.getMenuIds().length > 0) {
            for (Long menuId : vo.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                Db.save(rm);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(SysRoleVo vo) {
        SysRole role = Db.lambdaQuery(SysRole.class)
                .eq(SysRole::getId, vo.getId())
                .one();
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        role.setName(vo.getName());
        role.setType(vo.getType());
        role.setSort(vo.getSort());
        role.setStatus(vo.getStatus());
        role.setRemark(vo.getRemark());
        Db.updateById(role);

        // 更新菜单
        if (vo.getMenuIds() != null) {
            // 删除原有菜单
            Db.lambdaUpdate(SysRoleMenu.class)
                    .eq(SysRoleMenu::getRoleId, vo.getId())
                    .remove();
            // 分配新菜单
            for (Long menuId : vo.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(vo.getId());
                rm.setMenuId(menuId);
                Db.save(rm);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 删除角色菜单关联
        Db.lambdaUpdate(SysRoleMenu.class)
                .eq(SysRoleMenu::getRoleId, id)
                .remove();

        // 逻辑删除角色
        return Db.lambdaUpdate(SysRole.class)
                .eq(SysRole::getId, id)
                .set(SysRole::getDeleted, 1)
                .update();
    }

    @Override
    public Long[] getMenuIds(Long roleId) {
        List<SysRoleMenu> roleMenus = Db.lambdaQuery(SysRoleMenu.class)
                .eq(SysRoleMenu::getRoleId, roleId)
                .list();
        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .toArray(Long[]::new);
    }

    @Override
    @Transactional
    public boolean assignMenus(Long roleId, Long[] menuIds) {
        // 删除原有菜单
        Db.lambdaUpdate(SysRoleMenu.class)
                .eq(SysRoleMenu::getRoleId, roleId)
                .remove();

        // 分配新菜单
        if (menuIds != null && menuIds.length > 0) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                Db.save(rm);
            }
        }

        return true;
    }

    /**
     * 转换为 VO
     */
    private SysRoleVo convertToVo(SysRole role) {
        SysRoleVo vo = new SysRoleVo();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setType(role.getType());
        vo.setSort(role.getSort());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());

        // 查询菜单信息
        List<SysRoleMenu> roleMenus = Db.lambdaQuery(SysRoleMenu.class)
                .eq(SysRoleMenu::getRoleId, role.getId())
                .list();

        if (!roleMenus.isEmpty()) {
            Set<Long> menuIds = roleMenus.stream()
                    .map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toSet());
            vo.setMenuIds(menuIds.toArray(new Long[0]));
        }

        return vo;
    }
}