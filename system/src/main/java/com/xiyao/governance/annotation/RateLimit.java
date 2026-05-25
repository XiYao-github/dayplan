package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * <p>
 * 使用令牌桶算法实现基于每秒令牌数的限流控制。
 * 标注在 Controller 或 Service 方法上，自动限制接口调用频率。
 *
 * <p>
 * <b>算法说明：</b>
 * 令牌桶算法以固定速率向桶中添加令牌，
 * 请求时从桶中获取令牌，获取到才放行。
 * 桶有最大容量，超出容量的令牌会被丢弃。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @RateLimit(permitsPerSecond = 100, maxBurstRequests = 50)
 * @GetMapping("/api/user/{id}")
 * public Result&lt;User&gt; getUser(@PathVariable Long id) {
 *     return Result.ok(userService.getById(id));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.ratelimit.RateLimitAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 每秒令牌数
     * <p>
     * 令牌桶的令牌补充速率，即接口每秒允许的最大调用次数。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 每秒允许的请求数
     */
    double permitsPerSecond() default -1;

    /**
     * 突发容量
     * <p>
     * 令牌桶的最大容量，允许一次性处理的最大请求数。
     * 当有大量突发请求时，最多保留 burstCapacity 个令牌。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 令牌桶最大容量
     */
    int maxBurstRequests() default -1;

    /**
     * 限流后的提示消息
     * <p>
     * 当请求被限流拒绝时返回给客户端的错误消息。
     * 默认空字符串表示使用默认提示。
     *
     * @return 限流提示消息
     */
    String message() default "";
}