package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 系统菜单
 * </p>
 *
 * @author xiyao
 * @since 2026-06-09
 */
@Data
@TableName("sys_menu")
@Accessors(chain = true)
public class SysMenu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单标题
     */
    @TableField("title")
    private String title;

    /**
     * 菜单名称
     */
    @TableField("name")
    private String name;

    /**
     * 菜单类型(0.目录 1.菜单 2.按钮)
     */
    @TableField("type")
    private Integer type;

    /**
     * 菜单路径
     */
    @TableField("path")
    private String path;

    /**
     * 权限标识
     */
    @TableField("perms")
    private String perms;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 顺序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态(0.停用 1.正常)
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁
     */
    @Version
    @TableField("version")
    private Integer version;
}
