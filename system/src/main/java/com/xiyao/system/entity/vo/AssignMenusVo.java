package com.xiyao.system.entity.vo;

import lombok.Data;

/**
 * 角色分配菜单请求对象
 *
 * @author xiyao
 */
@Data
public class AssignMenusVo {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID数组
     */
    private Long[] menuIds;
}