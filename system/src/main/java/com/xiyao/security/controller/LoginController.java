package com.xiyao.security.controller;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.details.UserVo;
import com.xiyao.security.utils.JwtUtils;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 登录控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnBean(AuthenticationManager.class)
public class LoginController extends MyBaseController {

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserVo user) {
        // 获取用户名和密码
        String username = user.getUsername();
        String password = user.getPassword();
        // 创建 UsernamePasswordAuthenticationToken 对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        // 使用 AuthenticationManager 认证
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功，获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 生成 token 返回
        return success(jwtUtils.getToken(loginUser));
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserVo user) {
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
        newUser.setStatus(1);
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        // 保存用户
        Db.save(newUser);
        // 默认分配普通用户角色(测试)
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(2L);
        Db.save(userRole);
        return success();
    }

    /**
     * 退出登录
     */
    @DeleteMapping("/logout")
    public Result logout(HttpServletRequest request) {
        // 获取 token
        String token = jwtUtils.getHeaderToken(request);
        // 删除 token
        return jwtUtils.removeToken(token) ? success() : error("退出登录失败");
    }
}