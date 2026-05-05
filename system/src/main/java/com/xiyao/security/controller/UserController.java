package com.xiyao.security.controller;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.utils.Result;
import com.xiyao.framework.base.BaseController;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户列表（分页 + 条件查询）
     */
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/list")
    public Result list() {
        List<SysUser> list = Db.lambdaQuery(SysUser.class).list();
        return success(list);
    }

    /**
     * 用户详情
     */
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        SysUser info = Db.getById(id, SysUser.class);
        if (info == null) {
            return error("用户不存在");
        }
        return success(info);
    }

    /**
     * 新增用户
     */
    @PreAuthorize("hasAuthority('sys:user:add')")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody SysUser user) {
        // 检查用户名是否已存在
        Long count = Db.lambdaQuery(SysUser.class).eq(SysUser::getUsername, user.getUsername()).count();
        if (count > 0) {
            return error("用户已存在");
        }
        // 注册新用户
        SysUser newUser = new SysUser();
        newUser.setUsername(user.getUsername());
        // 加密密码
        String encode = passwordEncoder.encode(user.getPassword());
        newUser.setPassword(encode);
        newUser.setNickName(user.getNickName());
        newUser.setMobile(user.getMobile());
        newUser.setEmail(user.getEmail());
        newUser.setStatus(1);
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        // 保存用户
        Db.save(newUser);
        // 默认分配普通用户角色（role_id=2），这里需要注入 SysUserRoleMapper 并插入关联
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(2L);
        Db.save(userRole);
        return success();
    }

    /**
     * 更新用户
     */
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return error("用户ID不能为空");
        }
        SysUser info = Db.getById(user.getId(), SysUser.class);
        if (info == null) {
            return error("用户不存在");
        }
        // 更新基本信息
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setUpdateTime(LocalDateTime.now());
        Db.updateById(user);
        return success();
    }

    /**
     * 删除用户（逻辑删除）
     */
    @PreAuthorize("hasAuthority('sys:user:delete')")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        SysUser info = Db.getById(id, SysUser.class);
        if (info == null) {
            return error("用户不存在");
        }
        Db.removeById(info);
        return success();
    }
}