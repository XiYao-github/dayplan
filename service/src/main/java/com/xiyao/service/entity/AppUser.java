package com.xiyao.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 小程序用户
 * </p>
 *
 * @author xiyao
 */
@Data
@TableName("app_user")
@Accessors(chain = true)
public class AppUser {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 微信openID
     */
    @TableField("openid")
    private String openid;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态(0.停用 1.正常)
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 逻辑删除(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
