package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 操作日志事件
 * <p>
 * 继承自 MyBaseEvent，作为 Spring Event 的事件载体。
 * LogAspect 在方法执行前后构建此事件，最终由 LogListener 异步保存到数据库。
 *
 * <p>
 * <b>事件流程：</b>
 * <ol>
 *     <li>LogAspect.doAround() 方法执行前：构建事件基本信息（用户、模块、方法）</li>
 *     <li>LogAspect.doAround() 方法执行后：根据结果设置状态、返回数据</li>
 *     <li>finally 块：设置耗时，发布时间</li>
 *     <li>LogListener 监听并异步保存日志</li>
 * </ol>
 *
 * @author xiyao
 * @see com.xiyao.log.aspect.LogAspect
 * @see com.xiyao.log.listener.LogListener
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogOperationEvent extends MyBaseEvent {

    /**
     * 用户 ID
     * <p>
     * 从 SecurityUtils.getUserId() 获取当前登录用户 ID。
     */
    private Long userId;

    /**
     * 用户账号
     * <p>
     * 从 SecurityUtils.getUsername() 获取当前登录用户名。
     */
    private String username;

    /**
     * 三员类型
     * <p>
     * 记录操作用户的三员身份：
     * <ul>
     *     <li>0：普通用户</li>
     *     <li>1：系统管理员</li>
     *     <li>2：安全管理员</li>
     *     <li>3：审计管理员</li>
     * </ul>
     */
    private Integer adminType;

    /**
     * 操作方法
     * <p>
     * 格式：类名.方法名
     * 例如：UserController.addUser
     */
    private String method;

    /**
     * 操作模块
     * <p>
     * 与 @AuditLog 注解的 module() 对应，
     * 用于日志分类和检索。
     */
    private String module;

    /**
     * 操作类型
     * <p>
     * 对应 OperationType 枚举的 ordinal 值：
     * <ul>
     *     <li>0：其他</li>
     *     <li>1：查询</li>
     *     <li>2：新增</li>
     *     <li>3：更新</li>
     *     <li>4：删除</li>
     *     <li>5：导出</li>
     *     <li>6：导入</li>
     * </ul>
     */
    private Integer type;

    /**
     * 操作时间
     * <p>
     * 记录操作发生的时间戳。
     */
    private LocalDateTime time;

    /**
     * 操作状态
     * <p>
     * 标识操作是否成功：
     * <ul>
     *     <li>0：失败</li>
     *     <li>1：成功</li>
     * </ul>
     */
    private Integer status;

    /**
     * 提示消息
     * <p>
     * 操作结果的描述信息，
     * 成功时为"操作成功"，失败时为异常消息摘要。
     */
    private String message;

    /**
     * 请求参数
     * <p>
     * 序列化后的方法参数 JSON，
     * 仅在 @AuditLog(isSaveRequestData = true) 时填充。
     */
    private String requestParam;

    /**
     * 返回结果
     * <p>
     * 序列化后的方法返回值 JSON，
     * 仅在 @AuditLog(isSaveResponseData = true) 时填充。
     */
    private String returnResult;

    /**
     * 消耗时间
     * <p>
     * 方法执行的耗时，单位毫秒。
     */
    private Long costTime;
}