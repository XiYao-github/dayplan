package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 认证日志事件
 * <p>
 * 用于记录登录、登出、注册等认证操作。
 * 继承 MyBaseEvent，自动获取请求信息（IP、UA、设备等）。
 * 由 LogListener 异步保存到 log_login 表，带 SM3 哈希链防篡改。
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogLoginEvent extends MyBaseEvent {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 认证类型
     * <p>
     * 对应 OperationType 的 LOGIN/LOGOUT/REGISTER：
     * <ul>
     *     <li>LOGIN = 7：登录</li>
     *     <li>LOGOUT = 8：登出</li>
     *     <li>REGISTER = 9：注册</li>
     * </ul>
     */
    private Integer authType;

    /**
     * 认证状态
     * <p>
     * 0 = FAIL（失败），1 = SUCCESS（成功）
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 认证时间
     */
    private LocalDateTime loginTime;

    /**
     * 链路追踪 ID
     */
    private String traceId;
}