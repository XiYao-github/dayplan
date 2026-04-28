package com.xiyao.framework.security;

import com.xiyao.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@RestController
public class SysLoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 登录接口
     * @param loginBody 包含 username, password
     * @return { token: "xxx" }
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginBody) {
        String username = loginBody.get("username");
        String password = loginBody.get("password");

        // 调用 Spring Security 认证
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 认证成功，生成 token
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenService.createToken(loginUser);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return result;
    }

    /**
     * 获取当前用户信息（含权限）
     */
    @GetMapping("/getInfo")
    public Map<String, Object> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Map<String, Object> result = new HashMap<>();
        result.put("user", loginUser.getUser());
        result.put("permissions", loginUser.getPermissions());
        return result;
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            tokenService.deleteToken(token);
        }
        Map<String, String> result = new HashMap<>();
        result.put("msg", "登出成功");
        return result;
    }
}