package com.xiyao.security.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.security.details.LoginUser;
import com.xiyao.system.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户认证和授权信息加载服务
 * <p>
 * 实现 Spring Security 的 UserDetailsService 接口，
 * 用于根据用户名查询用户信息和权限，构建认证所需的 LoginUser 对象。
 *
 * <p>
 * <b>职责说明：</b>
 * <ul>
 *     <li>根据用户名查询用户基本信息（ID、密码、状态）</li>
 *     <li>查询用户关联的角色和菜单权限</li>
 *     <li>查询用户的三员类型</li>
 *     <li>构建包含完整权限信息的 LoginUser 对象</li>
 * </ul>
 *
 * <p>
 * <b>数据查询流程：</b>
 * <ol>
 *     <li>查询 sys_user 表获取用户基本信息</li>
 *     <li>查询 sys_user_role 表获取用户关联的角色</li>
 *     <li>查询 sys_role_menu 表获取角色关联的菜单</li>
 *     <li>查询 sys_menu 表获取菜单的权限标识</li>
 *     <li>查询 sys_role 表获取角色的三员类型</li>
 * </ol>
 *
 * <p>
 * <b>异常处理：</b>
 * <ul>
 *     <li>用户名不存在：抛出 UsernameNotFoundException</li>
 *     <li>用户被禁用（status=0）：抛出 UsernameNotFoundException</li>
 * </ul>
 *
 * @author xiyao
 * @see UserDetailsService
 * @see LoginUser
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 根据用户名加载用户信息和权限
     * <p>
     * 这是 UserDetailsService 接口的核心方法，
     * Spring Security 在认证时会调用此方法获取用户详情。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>查询用户基本信息，验证用户状态</li>
     *     <li>查询用户关联的角色列表</li>
     *     <li>查询角色关联的菜单列表</li>
     *     <li>查询菜单的权限标识集合</li>
     *     <li>查询角色的三员类型</li>
     *     <li>构建 LoginUser 对象返回</li>
     * </ol>
     *
     * @param username 用户名
     * @return UserDetails 实现类（此处为 LoginUser），包含用户信息和权限
     * @throws UsernameNotFoundException 用户不存在或已被禁用
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ========== 第1步：查询用户基本信息 ==========
        // 只查询必要的字段：ID、用户名、密码、状态
        SysUser user = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)  // 未被删除
                .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword, SysUser::getStatus)
                .one();

        // ========== 第2步：验证用户状态 ==========
        if (ObjectUtil.isEmpty(user)) {
            // 用户不存在
            throw new UsernameNotFoundException("用户不存在");
        } else if (user.getStatus() == 0) {
            // 用户被禁用
            throw new UsernameNotFoundException("用户被禁用");
        }

        // ========== 第3步：查询用户关联的角色 ID 列表 ==========
        // 通过 sys_user_role 中间表查询用户关联的所有角色
        Set<Long> roleIdList = Db.lambdaQuery(SysUserRole.class)
                .eq(SysUserRole::getUserId, user.getId())
                .select(SysUserRole::getRoleId)
                .list()
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());

        // ========== 第4步：查询角色关联的菜单 ID 列表 ==========
        // 通过 sys_role_menu 中间表查询角色关联的所有菜单
        Set<Long> menuIdList = Db.lambdaQuery(SysRoleMenu.class)
                .in(SysRoleMenu::getRoleId, roleIdList)
                .select(SysRoleMenu::getMenuId)
                .list()
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        // ========== 第5步：查询用户权限标识 ==========
        // 从 sys_menu 表查询菜单的权限标识
        // 权限标识格式如：system:user:list, system:user:add
        Set<String> perms = Db.lambdaQuery(SysMenu.class)
                .in(SysMenu::getId, menuIdList)
                .select(SysMenu::getPerms)
                .list()
                .stream()
                .map(SysMenu::getPerms)
                .filter(StrUtil::isNotBlank)  // 过滤空权限
                .collect(Collectors.toSet());

        // ========== 第6步：查询用户关联的三员类型 ==========
        // 从 sys_role 表查询角色类型
        // 用户可能关联多个角色，取最大类型值
        Set<Integer> adminTypes = Db.lambdaQuery(SysRole.class)
                .in(SysRole::getId, roleIdList)
                .select(SysRole::getType)
                .list()
                .stream()
                .map(SysRole::getType)
                .collect(Collectors.toSet());

        // 取最大角色类型（用户只会分配一种角色类型）
        // 0：普通用户，1：系统管理员，2：安全管理员，3：审计管理员
        Integer adminType = adminTypes.stream().max(Integer::compare).orElse(0);

        // ========== 第7步：构造 LoginUser 对象返回 ==========
        // LoginUser 包含用户ID、三员类型、用户实体、权限集合
        return new LoginUser(user.getId(), adminType, user, perms);
    }
}