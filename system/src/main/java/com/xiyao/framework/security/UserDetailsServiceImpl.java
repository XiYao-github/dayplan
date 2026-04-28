package com.xiyao.framework.security;

import com.xiyao.system.entity.SysUser;
import com.xiyao.system.mapper.SysMenuMapper;
import com.xiyao.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户验证处理类（UserDetailsService 实现）
 *
 * 作用：Spring Security 调用此类的 loadUserByUsername 从数据库加载用户信息。
 * 认证流程中，AuthenticationManager 会通过它获取用户详情，然后比对密码。
 *
 * 调用链：
 *   /login 请求 → SysLoginService → authenticationManager.authenticate()
 *   → DaoAuthenticationProvider → 调用此方法 → 返回 LoginUser
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户基本信息
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        if ("1".equals(user.getStatus())) {
            throw new UsernameNotFoundException("账户已停用：" + username);
        }

        // 2. 查询用户权限标识（perms）
        Set<String> perms = menuMapper.selectPermsByUserId(user.getId());
        if (perms == null) {
            perms = new HashSet<>();
        }

        // 3. 构造 LoginUser 对象返回
        return new LoginUser(user, perms);
    }
}