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
 * <p>
 * 提供用户管理的 CRUD 操作，包括查询、创建、更新、删除、角色分配等。
 * 所有接口需要三员权限（系统管理员或安全管理员）才能访问。
 *
 * <p>
 * <b>权限说明：</b>
 * <ul>
 *     <li>@PreAuthorize("@ss.hasAnyAdmin()")：系统管理员或安全管理员可访问</li>
 *     <li>@PreAuthorize("@ss.isSystemAdmin()")：仅系统管理员可访问（如重置密码）</li>
 * </ul>
 *
 * <p>
 * <b>日志记录：</b>
 * 所有写操作（INSERT/UPDATE/DELETE）都会通过 @Log 注解记录操作日志。
 *
 * @author xiyao
 * @see ISysUserService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends MyBaseController {

    /** 用户服务接口 */
    private final ISysUserService userService;

    /**
     * 查询用户列表
     * <p>
     * 支持分页查询和条件筛选，返回符合条件的用户列表。
     * 仅三员权限可访问。
     *
     * @param query 查询条件（包含分页参数）
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result list(SysUserVo query) {
        return ok(userService.list(query));
    }

    /**
     * 根据 ID 查询用户详情
     * <p>
     * 返回指定用户的完整信息。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result getById(@PathVariable Long id) {
        return ok(userService.getById(id));
    }

    /**
     * 创建用户
     * <p>
     * 创建新用户账号，默认分配普通用户角色。
     * 密码会被 BCrypt 加密存储。
     *
     * @param vo 用户信息（包含用户名、密码等）
     * @return 成功返回 null
     */
    @PostMapping
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.INSERT)
    public Result create(@RequestBody SysUserVo vo) {
        return ok(userService.create(vo));
    }

    /**
     * 更新用户
     * <p>
     * 修改用户信息，如昵称、手机号等。
     * 用户名不允许修改。
     *
     * @param vo 用户信息
     * @return 成功返回 null
     */
    @PutMapping
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result update(@RequestBody SysUserVo vo) {
        return ok(userService.update(vo));
    }

    /**
     * 删除用户
     * <p>
     * 执行逻辑删除（deleted 标志设为 1），而非物理删除。
     * 删除后用户无法登录。
     *
     * @param id 用户 ID
     * @return 成功返回 null
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.DELETE)
    public Result delete(@PathVariable Long id) {
        return ok(userService.delete(id));
    }

    /**
     * 分配角色
     * <p>
     * 为用户分配一个或多个角色，替换原有角色。
     * 角色决定用户的权限范围。
     *
     * @param vo 包含用户 ID 和角色 ID 列表
     * @return 成功返回 null
     */
    @PutMapping("/assign-roles")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result assignRoles(@RequestBody AssignRolesVo vo) {
        return ok(userService.assignRoles(vo.getUserId(), vo.getRoleIds()));
    }

    /**
     * 重置密码
     * <p>
     * 仅系统管理员可执行此操作。
     * 将用户密码重置为指定值（已加密）。
     *
     * @param vo 包含用户 ID 和新密码
     * @return 成功返回 null
     */
    @PutMapping("/reset-pwd")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result resetPassword(@RequestBody SysUserVo vo) {
        return ok(userService.resetPassword(vo.getId(), vo.getPassword()));
    }

    /**
     * 修改用户状态
     * <p>
     * 启用或停用用户账号。
     * 停用后用户无法登录。
     *
     * @param vo 包含用户 ID 和新状态
     * @return 成功返回 null
     */
    @PutMapping("/status")
    @PreAuthorize("@ss.hasAnyAdmin()")
    @Log(module = "用户管理", type = OperationType.UPDATE)
    public Result updateStatus(@RequestBody SysUserVo vo) {
        return ok(userService.updateStatus(vo.getId(), vo.getStatus()));
    }
}