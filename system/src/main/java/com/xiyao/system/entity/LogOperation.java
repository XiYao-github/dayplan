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
 * @since 2026-06-09
 */
@Data
@Accessors(chain = true)
@TableName("log_operation")
public class LogOperation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 操作模块(全类名.方法名)
     */
    @TableField("module")
    private String module;

    /**
     * 操作类型(0.其它 1.查询 2.新增 3.更新 4.删除 5.导出 6.导入)
     */
    @TableField("type")
    private Integer type;

    /**
     * 操作状态(0.失败 1.成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 操作时间
     */
    @TableField("time")
    private LocalDateTime time;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * 请求方式
     */
    @TableField("method")
    private String method;

    /**
     * 请求url
     */
    @TableField("url")
    private String url;

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
     * 消耗时间(毫秒)
     */
    @TableField("cost")
    private Long cost;

    /**
     * 客户端ip
     */
    @TableField("ip")
    private String ip;

    /**
     * ip归属地
     */
    @TableField("location")
    private String location;

    /**
     * 系统类型
     */
    @TableField("os")
    private String os;

    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;

    /**
     * 平台类型
     */
    @TableField("platform")
    private String platform;

    /**
     * 链路追踪ID
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 哈希值
     */
    @TableField("hash")
    private String hash;

    /**
     * 上一条哈希值
     */
    @TableField("prev_hash")
    private String prevHash;
}
