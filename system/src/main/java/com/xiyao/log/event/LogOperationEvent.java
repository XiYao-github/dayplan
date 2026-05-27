package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 操作日志事件
 * <p>
 * 继承 MyBaseEvent，自动获取请求信息（IP、UA、设备等）。
 * 根据 logType 决定存入哪张表（操作日志或审计日志）。
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogOperationEvent extends MyBaseEvent {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 三员类型（0=普通用户 1=系统管理员 2=安全管理员 3=审计管理员）
     */
    private Integer adminType;

    /**
     * 操作方法（类名.方法名）
     */
    private String method;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型（对应 OperationType 枚举的 ordinal）
     */
    private Integer type;

    /**
     * 日志类型（0=操作日志 1=审计日志）
     */
    private Integer logType;

    /**
     * 操作时间
     */
    private LocalDateTime time;

    /**
     * 操作状态（0=失败 1=成功）
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 请求参数（JSON 字符串）
     */
    private String requestParam;

    /**
     * 返回结果（JSON 字符串）
     */
    private String returnResult;

    /**
     * 消耗时间（毫秒）
     */
    private Long costTime;

}