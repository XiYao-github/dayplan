package com.xiyao.system.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单查询/响应对象
 *
 * @author xiyao
 */
@Data
public class SysMenuVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型(0.目录 1.菜单 2.按钮)
     */
    private Integer type;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0.停用 1.正常)
     */
    private Integer status;

    /**
     * 子菜单
     */
    private List<SysMenuVo> children;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}