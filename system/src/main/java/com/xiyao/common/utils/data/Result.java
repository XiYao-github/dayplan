package com.xiyao.common.utils.data;

import lombok.Data;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 前后端分离架构下，所有接口统一返回此格式：
 * <pre>{@code
 * {
 *     "code": 200,
 *     "msg": "请求成功",
 *     "data": { ... },
 *     "traceId": "abc123"
 * }
 * }</pre>
 *
 * <p>
 * <b>状态码说明：</b>
 * <ul>
 *     <li>200：成功</li>
 *     <li>401：未授权（未登录或登录失效）</li>
 *     <li>403：禁止访问（无权限）</li>
 *     <li>500：系统错误或业务错误</li>
 * </ul>
 *
 * @author xiyao
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码（HTTP 200）
     */
    public static final int OK = HttpStatus.OK.value();

    /**
     * 失败状态码（HTTP 500）
     */
    public static final int ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 响应编码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 全链路追踪标识
     */
    private String traceId;

    // ==================== 成功响应工厂方法 ====================

    /**
     * 返回成功响应（无数据）
     * <p>
     * 使用默认消息"请求成功"
     *
     * @param <T> 响应数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok() {
        return ok("请求成功");
    }

    /**
     * 返回成功响应（带消息）
     *
     * @param msg 成功消息
     * @param <T> 响应数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(String msg) {
        return ok(msg, null);
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 业务数据
     * @param <T>  响应数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(T data) {
        return ok("请求成功", data);
    }

    /**
     * 返回成功响应（带消息和数据）
     *
     * @param msg  成功消息
     * @param data 业务数据
     * @param <T>  响应数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(String msg, T data) {
        return result(OK, msg, data);
    }

    // ==================== 失败响应工厂方法 ====================

    /**
     * 返回失败响应（默认消息）
     *
     * @param <T> 响应数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error() {
        return error("请求失败");
    }

    /**
     * 返回失败响应（带消息）
     *
     * @param msg 错误消息
     * @param <T> 响应数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String msg) {
        return result(ERROR, msg, null);
    }

    /**
     * 返回失败响应（带状态码和消息）
     *
     * @param code 状态码
     * @param msg  错误消息
     * @param <T>  响应数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return result(code, msg, null);
    }

    // ==================== 通用响应工厂方法 ====================

    /**
     * 返回响应（带状态码）
     *
     * @param code 状态码
     * @param msg  消息
     * @param <T>  响应数据类型
     * @return 结果
     */
    public static <T> Result<T> result(Integer code, String msg) {
        return result(code, msg, null);
    }

    /**
     * 返回响应（完整参数）
     *
     * @param code 状态码
     * @param msg  消息
     * @param data 业务数据
     * @param <T>  响应数据类型
     * @return 结果
     */
    public static <T> Result<T> result(Integer code, String msg, T data) {
        return getResult(code, msg, data);
    }

    // ==================== 内部构建方法 ====================

    /**
     * 构建响应结果
     * <p>
     *创建一个 Result 实例，设置状态码、消息、数据，并自动注入 traceId。
     *
     * @param code 状态码
     * @param msg  消息
     * @param data 业务数据
     * @param <T>  响应数据类型
     * @return Result 实例
     */
    private static <T> Result<T> getResult(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        // 从 MDC 中获取链路追踪 ID，用于串联日志
        result.setTraceId(MDC.get("traceId"));
        return result;
    }
}