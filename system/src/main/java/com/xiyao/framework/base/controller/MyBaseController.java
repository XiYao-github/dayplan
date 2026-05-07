package com.xiyao.framework.base.controller;

import com.xiyao.framework.utils.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller 基础类
 */
@Slf4j
public class MyBaseController {

    /**
     * 返回成功响应
     */
    public Result success() {
        return Result.success();
    }

    /**
     * 返回成功响应（数据）
     */
    public Result success(Object data) {
        return Result.success(data);
    }

    /**
     * 返回成功响应（消息，数据）
     */
    public Result success(String msg, Object data) {
        return Result.success(msg, data);
    }

    /**
     * 返回失败响应
     */
    public Result error() {
        return Result.error();
    }

    /**
     * 返回失败响应（消息）
     */
    public Result error(String message) {
        return Result.error(message);
    }

    /**
     * 返回失败响应（状态，消息）
     */
    public Result error(Integer code, String message) {
        return Result.error(code, message);
    }

}