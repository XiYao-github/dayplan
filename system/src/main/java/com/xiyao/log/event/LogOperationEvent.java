package com.xiyao.log.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志事件
 */

@Data
public class LogOperationEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
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
    private String operationModule;

    /**
     * 操作方法
     */
    private String operationMethod;

    /**
     * 操作类型(0.其它 1.查询 2.新增 3.更新 4.删除)
     */
    private Integer operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作状态(0.失败 1.成功)
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 请求url
     */
    private String requestUrl;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 返回结果
     */
    private String returnResult;

    /**
     * 消耗时间(毫秒)
     */
    private Long costTime;
}
