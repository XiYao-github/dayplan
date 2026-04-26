package com.xiyao.basic.utils;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Result implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int OK = HttpStatus.OK.value();

    /**
     * 失败
     */
    public static final int ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 编码
     */
    private Integer code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private Object data;

    public static Result success() {
        return success("请求成功");
    }

    public static Result success(String msg) {
        return success(msg, null);
    }

    public static Result success(Object data) {
        return success("请求成功", data);
    }

    public static Result success(String msg, Object data) {
        return success(OK, msg, data);
    }

    public static Result success(Integer code, String msg, Object data) {
        return result(code, msg, data);
    }

    public static Result error() {
        return error("请求失败");
    }

    public static Result error(String msg) {
        return error(msg, null);
    }

    public static Result error(String msg, Object data) {
        return error(ERROR, msg, data);
    }

    public static Result error(Integer code, String msg) {
        return error(code, msg, null);
    }

    public static Result error(Integer code, String msg, Object data) {
        return result(code, msg, data);
    }

    private static Result result(Integer code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }
}