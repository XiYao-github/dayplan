package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.data.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.service.ISysRoleService;
import com.xiyao.system.vo.AssignMenusVo;
import com.xiyao.system.vo.SysRoleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends MyBaseController {

    private final ISysRoleService roleService;

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result list(SysRoleVo query) {
        return ok(roleService.list(query));
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result getById(@PathVariable Long id) {
        return ok(roleService.getById(id));
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "角色管理", type = OperationType.INSERT)
    public Result create(@RequestBody SysRoleVo vo) {
        return ok(roleService.create(vo));
    }

    /**
     * 更新角色
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "角色管理", type = OperationType.UPDATE)
    public Result update(@RequestBody SysRoleVo vo) {
        return ok(roleService.update(vo));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "角色管理", type = OperationType.DELETE)
    public Result delete(@PathVariable Long id) {
        return ok(roleService.delete(id));
    }

    /**
     * 查询角色已分配菜单
     */
    @GetMapping("/menus/{roleId}")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result getMenuIds(@PathVariable Long roleId) {
        return ok(roleService.getMenuIds(roleId));
    }

    /**
     * 分配菜单
     */
    @PutMapping("/assign-menus")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "角色管理", type = OperationType.UPDATE)
    public Result assignMenus(@RequestBody AssignMenusVo vo) {
        return ok(roleService.assignMenus(vo.getRoleId(), vo.getMenuIds()));
    }
}