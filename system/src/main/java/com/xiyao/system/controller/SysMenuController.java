package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.service.ISysMenuService;
import com.xiyao.system.vo.SysMenuVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单管理控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends MyBaseController {

    private final ISysMenuService menuService;

    /**
     * 查询菜单列表（树形）
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result list(SysMenuVo query) {
        return ok(menuService.listTree(query));
    }

    /**
     * 根据ID查询菜单
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result getById(@PathVariable Long id) {
        return ok(menuService.getById(id));
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "菜单管理", type = OperationType.INSERT)
    public Result create(@RequestBody SysMenuVo vo) {
        return ok(menuService.create(vo));
    }

    /**
     * 更新菜单
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "菜单管理", type = OperationType.UPDATE)
    public Result update(@RequestBody SysMenuVo vo) {
        return ok(menuService.update(vo));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "菜单管理", type = OperationType.DELETE)
    public Result delete(@PathVariable Long id) {
        return ok(menuService.delete(id));
    }

    /**
     * 查询菜单下拉选项
     */
    @GetMapping("/options")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result options() {
        return ok(menuService.listOptions());
    }
}