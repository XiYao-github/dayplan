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
 * @since 2026-06-09
 */
@Data
@TableName("log_login")
@Accessors(chain = true)
public class LogLogin {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 认证类型(7.登录 8.登出 9.注册)
     */
    @TableField("type")
    private Integer type;

    /**
     * 认证状态(0.失败 1.成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 认证时间
     */
    @TableField("time")
    private LocalDateTime time;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * 客户端ip
     */
    @TableField("ip")
    private String ip;

    /**
     * ip归属地
     */
    @TableField("location")
    private String location;

    /**
     * 系统类型
     */
    @TableField("os")
    private String os;

    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;

    /**
     * 平台类型
     */
    @TableField("platform")
    private String platform;

    /**
     * 链路追踪ID
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 哈希值
     */
    @TableField("hash")
    private String hash;

    /**
     * 上一条哈希值
     */
    @TableField("prev_hash")
    private String prevHash;
}
