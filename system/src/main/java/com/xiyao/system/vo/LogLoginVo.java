package com.xiyao.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 认证日志视图对象
 *
 * @author xiyao
 */
@Data
public class LogLoginVo {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 认证类型（7=登录 8=登出 9=注册）
     */
    private Integer authType;

    /**
     * 认证类型描述
     */
    private String authTypeDesc;

    /**
     * 认证状态（0=失败 1=成功）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 平台类型
     */
    private String platform;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 认证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
