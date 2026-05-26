package com.xiyao.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.service.ISysUserService;
import com.xiyao.security.utils.SecurityUtils;
import com.xiyao.system.vo.SysUserVo;
import com.xiyao.system.entity.SysRole;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 *
 * @author xiyao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements ISysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public List<SysUserVo> list(SysUserVo query) {
        List<SysUser> users = Db.lambdaQuery(SysUser.class)
                .eq(ObjectUtil.isNotNull(query.getId()), SysUser::getId, query.getId())
                .like(ObjectUtil.isNotNull(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(ObjectUtil.isNotNull(query.getNickName()), SysUser::getNickName, query.getNickName())
                .eq(ObjectUtil.isNotNull(query.getStatus()), SysUser::getStatus, query.getStatus())
                .eq(SysUser::getDeleted, 0)
                .list();

        return users.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public SysUserVo getById(Long id) {
        SysUser user = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getId, id)
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            return null;
        }
        return convertToVo(user);
    }

    @Override
    @Transactional
    public boolean create(SysUserVo vo) {
        // 检查用户名唯一性
        Long count = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, vo.getUsername())
                .eq(SysUser::getDeleted, 0)
                .count();
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(vo.getUsername());
        // user.setPassword(passwordEncoder.encode(vo.getPassword() != null ? vo.getPassword() : "123456"));
        user.setNickName(vo.getNickName());
        user.setMobile(vo.getMobile());
        user.setEmail(vo.getEmail());
        user.setSex(vo.getSex());
        user.setAvatar(vo.getAvatar());
        user.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        user.setRemark(vo.getRemark());
        user.setDeleted(0);
        Db.save(user);

        // 分配角色
        if (vo.getRoleIds() != null && vo.getRoleIds().length > 0) {
            for (Long roleId : vo.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                Db.save(ur);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(SysUserVo vo) {
        SysUser user = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getId, vo.getId())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setNickName(vo.getNickName());
        user.setMobile(vo.getMobile());
        user.setEmail(vo.getEmail());
        user.setSex(vo.getSex());
        user.setAvatar(vo.getAvatar());
        user.setStatus(vo.getStatus());
        user.setRemark(vo.getRemark());
        Db.updateById(user);

        // 更新角色
        if (vo.getRoleIds() != null) {
            // 删除原有角色
            Db.lambdaUpdate(SysUserRole.class)
                    .eq(SysUserRole::getUserId, vo.getId())
                    .remove();
            // 分配新角色
            for (Long roleId : vo.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(vo.getId());
                ur.setRoleId(roleId);
                Db.save(ur);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 不能删除自己
        if (SecurityUtils.getUserId().equals(id)) {
            throw new RuntimeException("不能删除当前登录用户");
        }

        // 删除用户角色关联
        Db.lambdaUpdate(SysUserRole.class)
                .eq(SysUserRole::getUserId, id)
                .remove();

        // 逻辑删除用户
        return Db.lambdaUpdate(SysUser.class)
                .eq(SysUser::getId, id)
                .set(SysUser::getDeleted, 1)
                .update();
    }

    @Override
    @Transactional
    public boolean assignRoles(Long userId, Long[] roleIds) {
        // 删除原有角色
        Db.lambdaUpdate(SysUserRole.class)
                .eq(SysUserRole::getUserId, userId)
                .remove();

        // 分配新角色
        if (roleIds != null && roleIds.length > 0) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                Db.save(ur);
            }
        }

        return true;
    }

    @Override
    public boolean resetPassword(Long id, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return Db.lambdaUpdate(SysUser.class)
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, encodedPassword)
                .update();
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return Db.lambdaUpdate(SysUser.class)
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status)
                .update();
    }

    /**
     * 转换为 VO
     */
    private SysUserVo convertToVo(SysUser user) {
        SysUserVo vo = new SysUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickName(user.getNickName());
        vo.setMobile(user.getMobile());
        vo.setEmail(user.getEmail());
        vo.setSex(user.getSex());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setLoginIp(user.getLoginIp());
        vo.setLoginDate(user.getLoginDate());
        vo.setRemark(user.getRemark());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());

        // 查询角色信息
        List<SysUserRole> userRoles = Db.lambdaQuery(SysUserRole.class)
                .eq(SysUserRole::getUserId, user.getId())
                .list();

        if (!userRoles.isEmpty()) {
            Set<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toSet());

            List<SysRole> roles = Db.lambdaQuery(SysRole.class)
                    .in(SysRole::getId, roleIds)
                    .list();

            vo.setRoleIds(roleIds.toArray(new Long[0]));
            vo.setRoleNames(roles.stream().map(SysRole::getName).toArray(String[]::new));
        }

        return vo;
    }
}