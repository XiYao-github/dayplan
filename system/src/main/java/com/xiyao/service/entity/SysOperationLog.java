package com.xiyao.service.entity;

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
 * @since 2026-04-28
 */
@Data
@Accessors(chain = true)
@TableName("sys_operation_log")
public class SysOperationLog {

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
     * 操作描述
     */
    @TableField("operation")
    private String operation;

    /**
     * 请求url
     */
    @TableField("url")
    private String url;

    /**
     * 操作ip
     */
    @TableField("ipaddr")
    private String ipaddr;

    /**
     * 操作地点
     */
    @TableField("location")
    private String location;

    /**
     * 方法名称
     */
    @TableField("method_name")
    private String methodName;

    /**
     * 请求方式
     */
    @TableField("req_method")
    private String reqMethod;

    /**
     * 操作类型(0.其它 1.查询 2.新增 3.修改 4.删除)
     */
    @TableField("operation_type")
    private Integer operationType;

    /**
     * 请求参数
     */
    @TableField("param")
    private String param;

    /**
     * 返回结果
     */
    @TableField("result")
    private String result;

    /**
     * 状态(0.失败 1.成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误消息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 消耗时间(毫秒)
     */
    @TableField("cost_time")
    private Long costTime;
}
