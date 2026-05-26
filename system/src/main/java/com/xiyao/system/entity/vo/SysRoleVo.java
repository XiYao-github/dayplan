package com.xiyao.system.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色查询/响应对象
 *
 * @author xiyao
 */
@Data
public class SysRoleVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色类型(0.普通用户 1.系统管理员 2.安全管理员 3.审计管理员)
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0.停用 1.正常)
     */
    private Integer status;

    /**
     * 菜单ID列表
     */
    private Long[] menuIds;

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