package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 登录日志事件
 * <p>
 * 当用户登录成功或失败时发布此事件，由 {@link com.xiyao.log.listener.LogListener} 异步处理并保存日志。
 * <p>
 * 事件发布流程：
 * <ol>
 *     <li>登录服务调用 SecurityUtils 记录登录信息</li>
 *     <li>发布 LogLoginEvent 事件</li>
 *     <li>LogListener 监听并异步保存到数据库</li>
 * </ol>
 *
 * @author xiyao
 * @see com.xiyao.log.listener.LogListener
 * @see com.xiyao.system.entity.LogLogin
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogLoginEvent extends MyBaseEvent {

    /**
     * 用户ID
     * <p>
     * 登录成功的用户ID，登录失败时为 null。
     */
    private Long userId;

    /**
     * 用户账号
     * <p>
     * 登录时使用的用户名。
     */
    private String username;

    /**
     * 登录时间
     * <p>
     * 服务器接收登录请求的时间。
     */
    private LocalDateTime loginTime;

    /**
     * 登录状态
     * <p>
     * 使用 {@link com.xiyao.log.enums.OperationStatus} 的 ordinal() 值：
     * <ul>
     *     <li>0 = SUCCESS：登录成功</li>
     *     <li>1 = FAIL：登录失败</li>
     * </ul>
     */
    private Integer status;

    /**
     * 提示消息
     * <p>
     * 登录结果的描述信息，如"登录成功"、"密码错误"、"账号已被禁用"等。
     */
    private String message;

    /**
     * 客户端 IP 地址
     * <p>
     * 用户登录时的真实 IP 地址，用于安全审计和异地登录检测。
     */
    private String clientIp;

    /**
     * 操作系统
     * <p>
     * 用户客户端的操作系统信息，如"Windows 10"、"iOS 15"等。
     */
    private String os;

    /**
     * 浏览器
     * <p>
     * 用户客户端的浏览器信息，如"Chrome 100"、"Safari 15"等。
     */
    private String browser;

    /**
     * 平台类型
     * <p>
     * 用户登录的平台，如"Web"、"小程序"、"APP"等。
     */
    private String platform;
}