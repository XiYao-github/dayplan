package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 防止重复提交注解
 * <p>
 * 标注在 Controller 方法上，防止用户重复提交（如双击按钮、刷新页面）。
 * 使用 Redis 缓存记录请求标识，一定时间内的重复请求会被拒绝。
 *
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>请求进入时生成唯一标识：userId + method + 参数hash</li>
 *     <li>检查 Redis 中是否存在该标识</li>
 *     <li>存在则拒绝请求，提示"请勿重复提交"</li>
 *     <li>不存在则存入 Redis（带过期时间），继续执行</li>
 * </ol>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @NoRepeatSubmit(key = "#user.id", expireSeconds = 5)
 * @PostMapping("/user")
 * public Result&lt;Void&gt; addUser(@CurrentUser LoginUser user, @RequestBody UserDTO dto) {
 *     return success(userService.add(dto));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.nosubmit.NoRepeatSubmitAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoRepeatSubmit {

    /**
     * 缓存键表达式
     * <p>
     * SpEL 表达式，用于生成 Redis 缓存的 key。
     * 默认使用简单类名+方法名作为前缀。
     * 例如：#user.id 表示从 user 参数获取 id
     *
     * @return SpEL 键表达式
     */
    String key() default "";

    /**
     * 锁定时间（秒）
     * <p>
     * 在此时间内相同请求会被视为重复提交。
     * 默认 3 秒，建议设置为业务操作耗时最长的 2-3 倍。
     *
     * @return 锁定时间，单位秒
     */
    int expireSeconds() default 3;

    /**
     * 提示消息
     * <p>
     * 当检测到重复提交时返回给用户的错误消息。
     *
     * @return 错误提示
     */
    String message() default "请勿重复提交，请稍后再试";
}