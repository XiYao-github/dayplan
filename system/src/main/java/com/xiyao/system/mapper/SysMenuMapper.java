package com.xiyao.system.mapper;

import com.xiyao.common.base.BaseMapper;
import com.xiyao.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * <p>
 * 系统菜单 Mapper 接口
 * </p>
 *
 * @author xiyao
 * @since 2026-04-28
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {


    /**
     * 根据用户ID查询权限标识集合（perms）
     * 注意：这里只查 perms 不为空的菜单权限，且关联的角色状态正常
     * @param userId 用户ID
     * @return 权限标识集合，例如 ["system:user:list", "system:role:add"]
     */
    @Select("SELECT DISTINCT m.perms " +
            "FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "LEFT JOIN sys_role r ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "AND m.perms IS NOT NULL AND m.perms != '' " +
            "AND r.status = 1 " +
            "AND m.deleted = 0")
    Set<String> selectPermsByUserId(@Param("userId") Long userId);

}
