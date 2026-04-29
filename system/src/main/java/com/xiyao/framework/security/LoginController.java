package com.xiyao.framework.security;

import com.xiyao.common.base.BaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LoginController extends BaseController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> map) {

        String username = map.get("username");
        String password = map.get("password");

        // 调用 AuthenticationManager 进行认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 认证成功，获取 UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 生成 JWT（你的工具类）
        String token = JwtUtils.generateToken(userDetails.getUsername());
        return success(token);
    }

    // hasRole('ADMIN')	        hasRole('ADMIN')	                用户拥有 ROLE_ADMIN 角色
    // hasAuthority('xxx')	    hasAuthority('system:user:add')	    用户拥有某个权限
    // hasAnyAuthority(...)	    hasAnyAuthority('a','b')	        有任意一个权限即可
    // @ss.hasPermi('xxx')      @ss.hasPermi('system:user:list')    自定义 Bean 方法
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public Result list() {
        List<SysUser> list = sysUserService.list();
        return success(list);
    }

}