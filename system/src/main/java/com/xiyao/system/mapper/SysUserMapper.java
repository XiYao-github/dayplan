package com.xiyao.system.mapper;

import com.xiyao.mybatisplus.base.mapper.MyBaseMapper;
import com.xiyao.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 系统用户 Mapper 接口
 * </p>
 *
 * @author xiyao
 * @since 2026-04-28
 */
@Mapper
public interface SysUserMapper extends MyBaseMapper<SysUser> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT id, username, password, nick_name, status " +
            "FROM sys_user " +
            "WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);
}
