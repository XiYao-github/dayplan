package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 重试注解
 * <p>
 * 当方法调用失败时，根据配置的重试策略自动进行重试。
 * 支持固定间隔和指数退避两种重试策略，提高系统容错能力。
 *
 * <p>
 * <b>重试策略：</b>
 * <ol>
 *     <li><b>固定间隔</b>：每次重试等待相同的时间间隔</li>
 *     <li><b>指数退避</b>：等待时间按倍数增长，如 1s, 2s, 4s, 8s...</li>
 * </ol>
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>执行目标方法</li>
 *     <li>如果成功，返回结果</li>
 *     <li>如果失败，检查是否应该重试（异常类型过滤）</li>
 *     <li>检查重试次数是否耗尽</li>
 *     <li>等待一段时间后再次尝试</li>
 *     <li>重复以上步骤直到成功或重试次数耗尽</li>
 * </ol>
 *
 * <p>
 * <b>异常过滤逻辑：</b>
 * <ul>
 *     <li>excludes（排除列表）优先级最高，在列表中的异常不会重试</li>
 *     <li>includes（包含列表）其次，仅列表中的异常会重试</li>
 *     <li>两者都为空，则所有异常都重试</li>
 * </ul>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>网络抖动导致的临时性失败</li>
 *     <li>下游服务短暂不可用</li>
 *     <li>数据库连接超时后重试</li>
 *     <li>需要最终一致性的操作</li>
 * </ul>
 *
 * <p>
 * <b>配置优先级：</b>
 * 注解属性值 &gt; 配置文件全局值（governance.retry.*）
 *
 * <p>
 * <b>使用示例 - 固定间隔重试：</b>
 * <pre>{@code
 * @Retryable(maxAttempts = 3, intervalMillis = 1000)
 * @GetMapping("/api/data")
 * public Result<String> fetchData() {
 *     return remoteService.get();
 * }
 * }</pre>
 *
 * <p>
 * <b>使用示例 - 指数退避重试：</b>
 * <pre>{@code
 * @Retryable(
 *     maxAttempts = 4,
 *     intervalMillis = 1000,
 *     multiplier = 2.0,
 *     includes = {RetryableException.class, TimeoutException.class}
 * )
 * @GetMapping("/api/external")
 * public Result<String> callExternal() {
 *     return externalService.getData();
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.retry.RetryAspect
 * @see com.xiyao.governance.core.retry.RetryContext
 * @see com.xiyao.governance.config.GovernanceProperties.RetryConfig
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {

    /**
     * 最大重试次数
     * <p>
     * 包含首次执行在内的最大执行次数。
     * 例如：设置为 3 表示最多执行 3 次（第 1 次 + 2 次重试）。
     * 默认 -1 表示使用配置文件中的全局配置（governance.retry.max-attempts）。
     *
     * @return 最大重试次数
     */
    int maxAttempts() default -1;

    /**
     * 重试间隔（毫秒）
     * <p>
     * 首次重试前的等待时间，也是固定间隔策略的间隔。
     * 默认 -1 表示使用配置文件中的全局配置（governance.retry.interval-millis）。
     *
     * @return 重试间隔（毫秒）
     */
    long intervalMillis() default -1;

    /**
     * 指数退避倍数
     * <p>
     * 重试间隔的指数增长倍数，仅在指数退避策略下生效。
     * 计算公式：interval * multiplier^(attempt - 1)
     * 例如：multiplier=2.0, interval=1000，则重试间隔为 1000, 2000, 4000...
     * 默认 -1 表示使用配置文件中的全局配置（governance.retry.multiplier）。
     *
     * @return 指数退避倍数
     */
    double multiplier() default -1;

    /**
     * 可重试的异常类型
     * <p>
     * 当目标方法抛出这些类型的异常时，会触发重试。
     * 如果为空数组，则结合 excludes 一起判断：
     * - 如果 excludes 也不为空，则仅重试 excludes 之外的异常
     * - 如果 excludes 为空，则重试所有异常
     * 支持异常继承关系，子类异常也会匹配。
     *
     * @return 可重试的异常类型数组
     */
    Class<? extends Throwable>[] includes() default {};

    /**
     * 不可重试的异常类型
     * <p>
     * 当目标方法抛出这些类型的异常时，不会进行重试。
     * 此配置优先级高于 includes。
     * 支持异常继承关系，子类异常也会匹配。
     *
     * @return 不可重试的异常类型数组
     */
    Class<? extends Throwable>[] excludes() default {};
}