package com.xiyao.common.utils;

import lombok.Data;
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
 *     "data": { ... }
 * }
 * }</pre>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 返回成功
 * return Result.success("操作成功", user);
 *
 * // 返回失败
 * return Result.error("用户名或密码错误");
 *
 * // 自定义错误码
 * return Result.error(401, "未授权");
 * }</pre>
 *
 * @author xiyao
 */
@Data
public class Result implements Serializable {

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
     * <p>
     * 200 表示成功，非 200 表示业务错误或系统错误
     */
    private Integer code;

    /**
     * 响应消息
     * <p>
     * 成功时返回 "请求成功"，失败时返回具体错误信息
     */
    private String msg;

    /**
     * 响应数据
     * <p>
     * 成功时返回业务数据，失败时可能返回 null 或错误详情
     */
    private Object data;

    // ==================== 成功响应 ====================

    /**
     * 返回成功（无数据）
     *
     * @return 成功结果
     */
    public static Result success() {
        return success("请求成功");
    }

    /**
     * 返回成功（带消息）
     *
     * @param msg 成功消息
     * @return 成功结果
     */
    public static Result success(String msg) {
        return success(msg, null);
    }

    /**
     * 返回成功（带数据）
     *
     * @param data 业务数据
     * @return 成功结果
     */
    public static Result success(Object data) {
        return success("请求成功", data);
    }

    /**
     * 返回成功（带消息和数据）
     *
     * @param msg  成功消息
     * @param data 业务数据
     * @return 成功结果
     */
    public static Result success(String msg, Object data) {
        return success(OK, msg, data);
    }

    /**
     * 返回成功（完整参数）
     *
     * @param code 状态码
     * @param msg  成功消息
     * @param data 业务数据
     * @return 成功结果
     */
    public static Result success(Integer code, String msg, Object data) {
        return result(code, msg, data);
    }

    // ==================== 失败响应 ====================

    /**
     * 返回失败（默认消息）
     *
     * @return 失败结果
     */
    public static Result error() {
        return error("请求失败");
    }

    /**
     * 返回失败（带消息）
     *
     * @param msg 错误消息
     * @return 失败结果
     */
    public static Result error(String msg) {
        return error(msg, null);
    }

    /**
     * 返回失败（带消息和数据）
     *
     * @param msg  错误消息
     * @param data 错误详情
     * @return 失败结果
     */
    public static Result error(String msg, Object data) {
        return error(ERROR, msg);
    }

    /**
     * 返回失败（带错误码和消息）
     *
     * @param code 错误码
     * @param msg  错误消息
     * @return 失败结果
     */
    public static Result error(Integer code, String msg) {
        return error(code, msg, null);
    }

    /**
     * 返回失败（完整参数）
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param data 错误详情
     * @return 失败结果
     */
    public static Result error(Integer code, String msg, Object data) {
        return result(code, msg, data);
    }

    // ==================== 内部方法 ====================

    /**
     * 构建响应结果
     *
     * @param code 状态码
     * @param msg  消息
     * @param data 数据
     * @return Result 实例
     */
    private static Result result(Integer code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }
}