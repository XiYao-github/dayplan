package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 操作记录
 * </p>
 *
 * @author xiyao
 * @since 2026-05-20
 */
@Data
@Accessors(chain = true)
@TableName("log_operation")
public class LogOperation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 三员类型(0.普通用户 1.系统管理员 2.安全管理员 3.审计管理员)
     */
    @TableField("admin_type")
    private Integer adminType;

    /**
     * 操作模块
     */
    @TableField("operation_module")
    private String operationModule;

    /**
     * 操作方法
     */
    @TableField("operation_method")
    private String operationMethod;

    /**
     * 操作类型(0.其它 1.查询 2.新增 3.更新 4.删除)
     */
    @TableField("operation_type")
    private Integer operationType;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 操作状态(0.失败 1.成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * 请求url
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方式
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 返回结果
     */
    @TableField("return_result")
    private String returnResult;

    /**
     * 消耗时间(毫秒)
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * ip地址
     */
    @TableField("ipaddr")
    private String ipaddr;

    /**
     * ip归属地
     */
    @TableField("location")
    private String location;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;
}
