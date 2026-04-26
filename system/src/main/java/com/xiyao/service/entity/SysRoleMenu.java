package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 角色关联菜单
 * </p>
 *
 * @author xiyao
 * @since 2026-04-26
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_menu")
public class SysRoleMenu {

    /**
     * 角色id
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 菜单id
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;
}
