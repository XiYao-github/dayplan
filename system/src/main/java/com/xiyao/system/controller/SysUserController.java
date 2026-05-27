package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.data.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.service.ISysUserService;
import com.xiyao.system.vo.AssignRolesVo;
import com.xiyao.system.vo.SysUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends MyBaseController {

    private final ISysUserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result list(SysUserVo query) {
        return ok(userService.list(query));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result getById(@PathVariable Long id) {
        return ok(userService.getById(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.INSERT)
    public Result create(@RequestBody SysUserVo vo) {
        return ok(userService.create(vo));
    }

    /**
     * 更新用户
     */
    @PutMapping
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result update(@RequestBody SysUserVo vo) {
        return ok(userService.update(vo));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.DELETE)
    public Result delete(@PathVariable Long id) {
        return ok(userService.delete(id));
    }

    /**
     * 分配角色
     */
    @PutMapping("/assign-roles")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result assignRoles(@RequestBody AssignRolesVo vo) {
        return ok(userService.assignRoles(vo.getUserId(), vo.getRoleIds()));
    }

    /**
     * 重置密码
     */
    @PutMapping("/reset-pwd")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result resetPassword(@RequestBody SysUserVo vo) {
        return ok(userService.resetPassword(vo.getId(), vo.getPassword()));
    }

    /**
     * 修改状态
     */
    @PutMapping("/status")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result updateStatus(@RequestBody SysUserVo vo) {
        return ok(userService.updateStatus(vo.getId(), vo.getStatus()));
    }
}