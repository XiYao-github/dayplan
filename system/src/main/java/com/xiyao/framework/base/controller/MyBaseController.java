package com.xiyao.framework.base.controller;

import com.xiyao.common.utils.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyBaseController {

    /**
     * 返回成功
     */
    public Result success() {
        return Result.success();
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