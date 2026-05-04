package com.xiyao.security.controller;

import com.xiyao.common.utils.Result;
import com.xiyao.system.entity.SysUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PreAuthorize("hasAuthority('user:add')")
    @PostMapping
    public Result addUser(@RequestBody SysUser user) {
        return Result.success("添加用户成功");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        return Result.success("删除用户成功");
    }
}