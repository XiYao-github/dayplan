package com.xiyao.framework.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public String list() {
        return "用户列表数据";
    }

    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @PostMapping
    public String add() {
        return "添加用户";
    }
}