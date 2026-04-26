package com.xiyao.basic.base;

import com.xiyao.basic.utils.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseController {

    /**
     * 返回成功
     */
    public Result success() {
        return Result.success();
    }

    /**
     * 返回成功消息
     */
    public Result success(String message) {
        return Result.success(message);
    }

    /**
     * 返回成功数据
     */
    public Result success(Object data) {
        return Result.success(data);
    }

    /**
     * 返回失败
     */
    public Result error() {
        return Result.error();
    }

    /**
     * 返回失败消息
     */
    public Result error(String message) {
        return Result.error(message);
    }

}