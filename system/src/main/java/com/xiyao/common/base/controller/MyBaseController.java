package com.xiyao.common.base.controller;

import com.xiyao.common.utils.data.Result;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.utils.SecurityUtils;

/**
 * Controller 基类
 * <p>
 * 提供统一的响应封装方法和用户信息获取方法，简化 Controller 开发。
 * 所有业务 Controller 应继承此类。
 *
 * <p>
 * <b>提供的方法：</b>
 * <ul>
 *     <li>ok() / ok(data) / ok(msg, data)：返回成功响应</li>
 *     <li>error() / error(message)：返回失败响应</li>
 *     <li>getLoginUser()：获取当前登录用户信息</li>
 *     <li>getUserId()：获取当前登录用户 ID</li>
 *     <li>getUsername()：获取当前登录用户名</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/user")
 * public class UserController extends MyBaseController {
 *
 *     @GetMapping("/{id}")
 *     public Result<User> getUser(@PathVariable Long id) {
 *         User user = userService.getById(id);
 *         return ok(user);
 *     }
 *
 *     @PostMapping
 *     public Result<Void> addUser(@RequestBody User user) {
 *         userService.save(user);
 *         return ok();
 *     }
 *
 *     @GetMapping("/me")
 *     public Result<Long> getCurrentUserId() {
 *         return ok(getUserId());
 *     }
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Result
 */
public class MyBaseController {

    // ==================== 成功响应方法 ====================

    /**
     * 返回成功响应（无数据）
     * <p>
     * 使用默认消息"请求成功"，适用于不需要返回数据的操作。
     *
     * @param <T> 响应数据类型
     * @return 成功结果 Result
     */
    public <T> Result<T> ok() {
        return Result.ok();
    }

    /**
     * 返回成功响应（带数据）
     * <p>
     * 使用默认消息"请求成功"，适用于查询等需要返回数据的操作。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果 Result
     */
    public <T> Result<T> ok(T data) {
        return Result.ok(data);
    }

    /**
     * 返回成功响应（带消息和数据）
     * <p>
     * 适用于需要自定义成功消息的场景，如"保存成功"、"删除成功"等。
     *
     * @param msg  成功消息
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果 Result
     */
    public <T> Result<T> ok(String msg, T data) {
        return Result.ok(msg, data);
    }

    // ==================== 失败响应方法 ====================

    /**
     * 返回失败响应（默认消息）
     * <p>
     * 使用默认消息"请求失败"，适用于通用错误或异常捕获场景。
     *
     * @param <T> 响应数据类型
     * @return 失败结果 Result
     */
    public <T> Result<T> error() {
        return Result.error();
    }

    /**
     * 返回失败响应（带消息）
     * <p>
     * 适用于业务校验失败、权限不足等明确知道错误原因的场景。
     *
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return 失败结果 Result
     */
    public <T> Result<T> error(String message) {
        return Result.error(message);
    }

    // ==================== 当前用户信息方法 ====================

    /**
     * 获取当前登录用户信息
     * <p>
     * 从 SecurityContext 中获取当前登录用户的完整信息。
     *
     * @return LoginUser 对象，包含用户 ID、用户名、角色等信息
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取当前登录用户 ID
     * <p>
     * 从登录用户信息中提取用户 ID，适用于需要记录操作人的场景。
     *
     * @return 当前登录用户的 ID
     */
    public Long getUserId() {
        // 获取登录用户信息并提取 ID
        return getLoginUser().getUserId();
    }

    /**
     * 获取当前登录用户名
     * <p>
     * 从登录用户信息中提取用户名，适用于需要显示操作人的场景。
     *
     * @return 当前登录用户的用户名
     */
    public String getUsername() {
        // 获取登录用户信息并提取用户名
        return getLoginUser().getUsername();
    }
}