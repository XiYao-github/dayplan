package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 用户关联角色
 * </p>
 *
 * @author xiyao
 * @since 2026-04-26
 */
@Data
@Accessors(chain = true)
@TableName("sys_user_role")
public class SysUserRole {

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色id
     */
    @TableField("role_id")
    private Long roleId;
}
