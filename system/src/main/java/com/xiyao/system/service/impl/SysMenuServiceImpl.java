package com.xiyao.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.service.ISysMenuService;
import com.xiyao.system.vo.SysMenuVo;
import com.xiyao.system.entity.SysMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现
 *
 * @author xiyao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements ISysMenuService {

    @Override
    public List<SysMenuVo> listTree(SysMenuVo query) {
        List<SysMenuVo> allMenus = list(query);
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenuVo> list(SysMenuVo query) {
        List<SysMenu> menus = Db.lambdaQuery(SysMenu.class)
                .eq(ObjectUtil.isNotNull(query.getId()), SysMenu::getId, query.getId())
                .eq(ObjectUtil.isNotNull(query.getParentId()), SysMenu::getParentId, query.getParentId())
                .like(ObjectUtil.isNotNull(query.getTitle()), SysMenu::getTitle, query.getTitle())
                .eq(ObjectUtil.isNotNull(query.getType()), SysMenu::getType, query.getType())
                .eq(ObjectUtil.isNotNull(query.getStatus()), SysMenu::getStatus, query.getStatus())
                .list();

        return menus.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public SysMenuVo getById(Long id) {
        SysMenu menu = Db.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getId, id)
                .one();
        if (menu == null) {
            return null;
        }
        return convertToVo(menu);
    }

    @Override
    public boolean create(SysMenuVo vo) {
        SysMenu menu = new SysMenu();
        menu.setParentId(vo.getParentId() != null ? vo.getParentId() : 0L);
        menu.setTitle(vo.getTitle());
        menu.setName(vo.getName());
        menu.setType(vo.getType());
        menu.setPath(vo.getPath());
        menu.setComponent(vo.getComponent());
        menu.setPerms(vo.getPerms());
        menu.setIcon(vo.getIcon());
        menu.setSort(vo.getSort() != null ? vo.getSort() : 0);
        menu.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        menu.setRemark(vo.getRemark());
        menu.setDeleted(0);
        Db.save(menu);

        return true;
    }

    @Override
    public boolean update(SysMenuVo vo) {
        SysMenu menu = Db.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getId, vo.getId())
                .one();
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        menu.setParentId(vo.getParentId());
        menu.setTitle(vo.getTitle());
        menu.setName(vo.getName());
        menu.setType(vo.getType());
        menu.setPath(vo.getPath());
        menu.setComponent(vo.getComponent());
        menu.setPerms(vo.getPerms());
        menu.setIcon(vo.getIcon());
        menu.setSort(vo.getSort());
        menu.setStatus(vo.getStatus());
        menu.setRemark(vo.getRemark());
        Db.updateById(menu);

        return true;
    }

    @Override
    public boolean delete(Long id) {
        // 检查是否有子菜单
        Long count = Db.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, 0)
                .count();
        if (count > 0) {
            throw new RuntimeException("存在子菜单，无法删除");
        }

        // 逻辑删除菜单
        return Db.lambdaUpdate(SysMenu.class)
                .eq(SysMenu::getId, id)
                .set(SysMenu::getDeleted, 1)
                .update();
    }

    @Override
    public List<SysMenuVo> listOptions() {
        List<SysMenu> menus = Db.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getDeleted, 0)
                .eq(SysMenu::getStatus, 1)
                .list();

        return menus.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    /**
     * 转换为 VO
     */
    private SysMenuVo convertToVo(SysMenu menu) {
        SysMenuVo vo = new SysMenuVo();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setTitle(menu.getTitle());
        vo.setName(menu.getName());
        vo.setType(menu.getType());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPerms(menu.getPerms());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setStatus(menu.getStatus());
        vo.setRemark(menu.getRemark());
        vo.setCreateTime(menu.getCreateTime());
        vo.setUpdateTime(menu.getUpdateTime());
        return vo;
    }

    /**
     * 构建树形结构
     */
    private List<SysMenuVo> buildTree(List<SysMenuVo> menus) {
        List<SysMenuVo> result = new ArrayList<>();
        List<SysMenuVo> rootMenus = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0L)
                .collect(Collectors.toList());

        for (SysMenuVo root : rootMenus) {
            root.setChildren(getChildren(root.getId(), menus));
            result.add(root);
        }

        return result;
    }

    /**
     * 递归获取子菜单
     */
    private List<SysMenuVo> getChildren(Long parentId, List<SysMenuVo> allMenus) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> m.setChildren(getChildren(m.getId(), allMenus)))
                .collect(Collectors.toList());
    }
}