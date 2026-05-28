package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 操作日志事件
 * <p>
 * 用于记录业务操作日志和审计日志，继承 MyBaseEvent 自动获取请求信息。
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
 * <b>日志分流：</b>
 * <ul>
 *     <li>logType = OPERATION：存入 log_operation 表（无哈希链）</li>
 *     <li>logType = AUDIT：存入 log_operation 表（有 SM3 哈希链）</li>
 * </ul>
 *
 * @author xiyao
 * @see MyBaseEvent
 * @see com.xiyao.log.listener.LogListener
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogOperationEvent extends MyBaseEvent {

    /**
     * 用户 ID
     * <p>
     * 当前操作用户的 ID，未登录则为 null。
     */
    private Long userId;

    /**
     * 用户账号
     * <p>
     * 当前操作用户的用户名，未登录则为 null。
     */
    private String username;

    /**
     * 三员类型
     * <p>
     * 用于等保合规的三员管理：
     * <ul>
     *     <li>0：普通用户</li>
     *     <li>1：系统管理员</li>
     *     <li>2：安全管理员</li>
     *     <li>3：审计管理员</li>
     * </ul>
     */
    private Integer adminType;

    /**
     * 操作方法（类名.方法名）
     * <p>
     * 格式：ControllerName.methodName
     * 例如：UserController.createUser
     */
    private String method;

    /**
     * 操作模块
     * <p>
     * 标识日志所属业务模块，如"用户管理"、"订单管理"等。
     */
    private String module;

    /**
     * 操作类型（对应 OperationType 枚举的 ordinal）
     * <p>
     * 用于分类日志：
     * <ul>
     *     <li>0=OTHER 1=QUERY 2=INSERT 3=UPDATE 4=DELETE 5=EXPORT 6=IMPORT</li>
     * </ul>
     */
    private Integer type;

    /**
     * 日志类型（对应 LogType 枚举的 ordinal）
     * <p>
     * 决定日志存储策略：
     * <ul>
     *     <li>0=OPERATION：无哈希链</li>
     *     <li>1=AUDIT：有 SM3 哈希链防篡改</li>
     * </ul>
     */
    private Integer logType;

    /**
     * 操作时间
     */
    private LocalDateTime time;

    /**
     * 操作状态（对应 OperationStatus 枚举的 ordinal）
     * <p>
     * <ul>
     *     <li>0=FAIL（失败）</li>
     *     <li>1=SUCCESS（成功）</li>
     * </ul>
     */
    private Integer status;

    /**
     * 提示消息
     * <p>
     * 成功时为"操作成功"，失败时为异常信息。
     */
    private String message;

    /**
     * 请求参数（JSON 字符串）
     * <p>
     * 方法参数序列化后的 JSON 字符串，isSaveRequestData=true 时填充。
     */
    private String requestParam;

    /**
     * 返回结果（JSON 字符串）
     * <p>
     * 方法返回值序列化后的 JSON 字符串，isSaveResponseData=true 时填充。
     */
    private String returnResult;

    /**
     * 消耗时间（毫秒）
     * <p>
     * 方法从执行到返回的总耗时。
     */
    private Long costTime;

}