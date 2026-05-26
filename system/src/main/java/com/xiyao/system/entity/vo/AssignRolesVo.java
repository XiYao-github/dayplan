package com.xiyao.system.entity.vo;

import lombok.Data;

/**
 * 用户分配角色请求对象
 *
 * @author xiyao
 */
@Data
public class AssignRolesVo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID数组
     */
    private Long[] roleIds;
}