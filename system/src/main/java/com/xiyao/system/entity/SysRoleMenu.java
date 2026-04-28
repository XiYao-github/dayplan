package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 角色关联菜单
 * </p>
 *
 * @author xiyao
 * @since 2026-04-28
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_menu")
public class SysRoleMenu {

    /**
     * 角色id
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 菜单id
     */
    @TableField("menu_id")
    private Long menuId;
}
