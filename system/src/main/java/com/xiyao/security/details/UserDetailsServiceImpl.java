package com.xiyao.security.details;

import com.xiyao.system.entity.SysUser;
import com.xiyao.system.mapper.SysMenuMapper;
import com.xiyao.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户基本信息
        SysUser user = userMapper.selectByUsername(username);

        // 查询用户权限标识
        Set<String> perms = menuMapper.selectPermsByUserId(user.getId());

        // 构造对象返回
        return new LoginUser(user, perms);
    }
}