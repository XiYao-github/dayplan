package com.xiyao.security.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.utils.Result;
import com.xiyao.framework.base.BaseController;
import com.xiyao.mybatisplus.utils.RedisUtils;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.JwtUtils;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class LoginController extends BaseController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result login(@RequestBody SysUser user) {

        String username = user.getUsername();
        String password = user.getPassword();

        // 调用 AuthenticationManager 进行认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功，获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 生成 JWT
        String loginUserKey = IdUtil.fastSimpleUUID();
        String token = jwtUtils.generateToken(loginUserKey);
        // 存储 token 到 Redis 中
        redisUtils.set(JwtUtils.LOGIN_USER_KEY + loginUserKey, loginUser, JwtUtils.SECONDS);
        // 返回 token
        return success(token);
    }


    @PostMapping("/register")
    public Result register(@RequestBody SysUser user) {
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

}