package com.xiyao.common.base.controller;

import com.xiyao.common.utils.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller 基类
 * <p>
 * 提供统一的响应封装方法，简化 Controller 开发。
 * 所有业务 Controller 应继承此类。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/user")
 * public class UserController extends MyBaseController {
 *
 *     @GetMapping("/{id}")
 *     public Result&lt;User&gt; getUser(@PathVariable Long id) {
 *         return success(userService.getById(id));
 *     }
 *
 *     @PostMapping
 *     public Result&lt;Void&gt; addUser(@RequestBody User user) {
 *         userService.save(user);
 *         return success();
 *     }
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Result
 */
@Slf4j
public class MyBaseController {

    /**
     * 返回成功响应（无数据）
     *
     * @return 成功结果，默认消息"请求成功"
     */
    public Result success() {
        return Result.success();
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public <T> Result success(T data) {
        return Result.success(data);
    }

    /**
     * 返回成功响应（带消息和数据）
     *
     * @param msg  成功消息
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public <T> Result success(String msg, T data) {
        return Result.success(msg, data);
    }

    /**
     * 返回失败响应（默认消息）
     *
     * @return 失败结果，默认消息"请求失败"
     */
    public Result error() {
        return Result.error();
    }

    /**
     * 返回失败响应（带消息）
     *
     * @param message 错误消息
     * @return 失败结果
     */
    public Result error(String message) {
        return Result.error(message);
    }

    /**
     * 返回失败响应（带状态码和消息）
     *
     * @param code    HTTP 状态码
     * @param message 错误消息
     * @return 失败结果
     */
    public Result error(Integer code, String message) {
        return Result.error(code, message);
    }
}