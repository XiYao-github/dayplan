package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 系统菜单
 * </p>
 *
 * @author xiyao
 * @since 2026-04-26
 */
@Data
@TableName("sys_menu")
@Accessors(chain = true)
public class SysMenu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单id
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
    private Byte type;

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
     * 组件路径
     */
    @TableField("component")
    private String component;

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
    private Byte status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除时间
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 删除标志(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("del_flag")
    private Byte delFlag;
}
