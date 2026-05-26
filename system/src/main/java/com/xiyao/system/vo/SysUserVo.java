package com.xiyao.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户查询/响应对象
 *
 * @author xiyao
 */
@Data
public class SysUserVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别(0.未知 1.男 2.女)
     */
    private Integer sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态(0.停用 1.正常)
     */
    private Integer status;

    /**
     * 角色ID列表
     */
    private Long[] roleIds;

    /**
     * 角色名称列表
     */
    private String[] roleNames;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

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