package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 操作日志事件
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogOperationEvent extends MyBaseEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 三员类型(0.普通用户 1.系统管理员 2.安全管理员 3.审计管理员)
     */
    private Integer adminType;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作方法（类名.方法名）
     */
    private String method;

    /**
     * 操作类型（0.其他 1.查询 2.新增 3.更新 4.删除 5.导出 6.导入）
     */
    private Integer type;

    /**
     * 操作时间
     */
    private LocalDateTime time;

    /**
     * 操作状态（0.失败 1.成功）
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 请求参数（请求行、请求体）
     */
    private String requestParam;

    /**
     * 返回结果
     */
    private String returnResult;

    /**
     * 消耗时间（毫秒）
     */
    private Long costTime;
}