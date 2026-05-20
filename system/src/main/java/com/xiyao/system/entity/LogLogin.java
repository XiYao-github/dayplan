package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 登录记录
 * </p>
 *
 * @author xiyao
 * @since 2026-05-20
 */
@Data
@TableName("log_login")
@Accessors(chain = true)
public class LogLogin {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登录状态(0.失败 1.成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * ip地址
     */
    @TableField("ipaddr")
    private String ipaddr;

    /**
     * ip归属地
     */
    @TableField("location")
    private String location;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;
}
