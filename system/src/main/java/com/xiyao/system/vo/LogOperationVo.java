package com.xiyao.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志视图对象
 *
 * @author xiyao
 */
@Data
public class LogOperationVo {

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
     * 三员类型（0=普通用户 1=系统管理员 2=安全管理员 3=审计管理员）
     */
    private Integer adminType;

    /**
     * 三员类型描述
     */
    private String adminTypeDesc;

    /**
     * 日志类型（0=操作日志 1=审计日志）
     */
    private Integer logType;

    /**
     * 日志类型描述
     */
    private String logTypeDesc;

    /**
     * 操作模块
     */
    private String operationModule;

    /**
     * 操作方法
     */
    private String operationMethod;

    /**
     * 操作类型（0=其它 1=查询 2=新增 3=更新 4=删除 5=导出 6=导入）
     */
    private Integer operationType;

    /**
     * 操作类型描述
     */
    private String operationTypeDesc;

    /**
     * 操作状态（0=失败 1=成功）
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
     * 请求参数
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

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * IP归属地
     */
    private String location;

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
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}
