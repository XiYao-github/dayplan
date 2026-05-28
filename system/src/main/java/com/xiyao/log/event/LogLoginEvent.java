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
 *
 * <p>
 * <b>继承说明：</b>
 * MyBaseEvent 构造函数会自动从 HttpServletRequest 中提取：
 * <ul>
 *     <li>网络信息：clientIp、clientPort、serverIp</li>
 *     <li>请求行：requestMethod、requestUrl、queryString</li>
 *     <li>请求头：userAgent、referer、origin、contentType</li>
 *     <li>设备信息：os、browser、platform</li>
 * </ul>
 *
 * <p>
 * <b>事件发布：</b>
 * 由 LoginController 在登录/注册/登出时通过 SpringUtil.publishEvent() 发布，
 * 由 LogListener 异步保存到 log_login 表。
 *
 * <p>
 * <b>哈希链说明：</b>
 * 认证日志强制使用 SM3 哈希链防篡改，每条记录的 hash 值包含：
 * 用户信息 + 操作信息 + IP/设备信息 + traceId + 时间 + 上一条记录的 hash
 *
 * @author xiyao
 * @see MyBaseEvent
 * @see com.xiyao.security.controller.LoginController
 * @see com.xiyao.log.listener.LogListener
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogLoginEvent extends MyBaseEvent {

    /**
     * 用户 ID
     * <p>
     * 当前操作用户的 ID，未登录（认证失败）时可能为 null。
     */
    private Long userId;

    /**
     * 用户账号
     * <p>
     * 当前操作用户的用户名，认证失败时记录尝试登录的用户名。
     */
    private String username;

    /**
     * 认证类型（对应 OperationType 枚举的 ordinal）
     * <p>
     * 标识认证操作的类型：
     * <ul>
     *     <li>LOGIN = 7：登录</li>
     *     <li>LOGOUT = 8：登出</li>
     *     <li>REGISTER = 9：注册</li>
     * </ul>
     */
    private Integer authType;

    /**
     * 认证状态（对应 OperationStatus 枚举的 ordinal）
     * <p>
     * <ul>
     *     <li>FAIL = 0：失败</li>
     *     <li>SUCCESS = 1：成功</li>
     * </ul>
     */
    private Integer status;

    /**
     * 提示消息
     * <p>
     * 成功时为"登录成功"等提示，失败时为异常信息。
     */
    private String message;

    /**
     * 认证时间
     */
    private LocalDateTime loginTime;

}