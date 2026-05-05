package com.xiyao.security.details;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.SysMenu;
import com.xiyao.system.entity.SysRoleMenu;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import com.xiyao.system.mapper.SysMenuMapper;
import com.xiyao.system.mapper.SysRoleMapper;
import com.xiyao.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户基本信息
        SysUser user = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword, SysUser::getStatus)
                .one();
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
        // 构造对象返回
        return new LoginUser(user.getId(), user, perms);
    }

}