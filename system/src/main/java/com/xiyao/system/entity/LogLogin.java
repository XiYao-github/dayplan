package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 认证日志（登录/登出/注册）
 * <p>
 * 记录认证操作，带 SM3 哈希链防篡改。
 *
 * @author xiyao
 */
@Data
@TableName("log_login")
@Accessors(chain = true)
public class LogLogin {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 认证类型（7=登录 8=登出 9=注册）
     */
    @TableField("auth_type")
    private Integer authType;

    /**
     * 认证状态（0=失败 1=成功）
     */
    @TableField("status")
    private Integer status;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * 客户端 IP
     */
    @TableField("ip")
    private String ip;

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
     * 平台类型
     */
    @TableField("platform")
    private String platform;

    /**
     * 链路追踪 ID
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 哈希值（SM3 防篡改）
     */
    @TableField("hash")
    private String hash;

    /**
     * 上一条哈希值（哈希链）
     */
    @TableField("prev_hash")
    private String prevHash;

    /**
     * 认证时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
}