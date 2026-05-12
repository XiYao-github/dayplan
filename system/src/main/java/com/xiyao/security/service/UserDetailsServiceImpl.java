package com.xiyao.security.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.security.details.LoginUser;
import com.xiyao.system.entity.*;
import com.xiyao.system.mapper.SysMenuMapper;
import com.xiyao.system.mapper.SysRoleMapper;
import com.xiyao.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现 UserDetailsService 接口，用于 Spring Security 获取用户信息和授权信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户基本信息
        SysUser user = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword, SysUser::getStatus)
                .one();
        // 判断用户状态
        if (ObjectUtil.isEmpty(user)) {
            throw new UsernameNotFoundException("用户不存在");
        } else if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户被禁用");
        }
        // 查询用户关联角色ID列表
        Set<Long> roleIdList = Db.lambdaQuery(SysUserRole.class)
                .eq(SysUserRole::getUserId, user.getId())
                .select(SysUserRole::getRoleId)
                .list().stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        // 查询角色关联菜单ID列表
        Set<Long> menuIdList = Db.lambdaQuery(SysRoleMenu.class)
                .in(SysRoleMenu::getRoleId, roleIdList)
                .select(SysRoleMenu::getMenuId)
                .list().stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        // 查询用户权限标识
        Set<String> perms = Db.lambdaQuery(SysMenu.class)
                .in(SysMenu::getId, menuIdList)
                .select(SysMenu::getPerms)
                .list().stream().map(SysMenu::getPerms).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        // 查询用户关联的三员类型
        Set<Integer> adminTypes = Db.lambdaQuery(SysRole.class)
                .in(SysRole::getId, roleIdList)
                .select(SysRole::getRoleType)
                .list().stream().map(SysRole::getRoleType).collect(Collectors.toSet());
        // 取最大角色类型（用户只会分配一种角色类型）
        Integer adminType = adminTypes.stream().max(Integer::compare).orElse(0);
        // 构造对象返回
        return new LoginUser(user.getId(), adminType, user, perms);
    }

}