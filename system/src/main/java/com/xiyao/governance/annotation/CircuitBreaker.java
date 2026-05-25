package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 熔断器注解
 * <p>
 * 基于失败率的三状态熔断器（Circuit Breaker）实现。
 * 当下游服务故障达到一定阈值时自动触发熔断，快速失败防止故障扩散。
 *
 * <p>
 * <b>熔断器三状态：</b>
 * <ol>
 *     <li><b>CLOSED（关闭）</b>：正常状态，统计失败率，未触发熔断</li>
 *     <li><b>OPEN（打开）</b>：熔断状态，所有请求直接失败，快速返回</li>
 *     <li><b>HALF_OPEN（半开）</b>：试探恢复，允许少量请求通过尝试恢复</li>
 * </ol>
 *
 * <p>
 * <b>状态转换逻辑：</b>
 * <pre>
 *  CLOSED ──(失败率达标)──&gt; OPEN
 *    ↑                      │
 *    │                      ↓
 *    └──(超时后)──&gt; HALF_OPEN ──(成功率达标)──&gt; CLOSED
 *                        │
 *                        └────(失败)────────&gt; OPEN
 * </pre>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>调用下游服务时，下游服务响应变慢或频繁超时</li>
 *     <li>防止级联故障，一个服务的问题不影响整个系统</li>
 *     <li>给下游服务恢复时间，避免被大量请求冲垮</li>
 * </ul>
 *
 * <p>
 * <b>配置优先级：</b>
 * 注解属性值 &gt; 配置文件全局值（governance.circuit-breaker.*）
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @CircuitBreaker(
 *     failureRateThreshold = 5,
 *     successRateThreshold = 3,
 *     windowSizeMillis = 60000,
 *     breakDurationMillis = 30000,
 *     errorRateThreshold = 50,
 *     minRequestNumber = 10
 * )
 * @GetMapping("/api/external")
 * public Result<String> callExternal() {
 *     return externalService.getData();
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.circuit.CircuitBreakerAspect
 * @see com.xiyao.governance.enums.CircuitState
 * @see com.xiyao.governance.config.GovernanceProperties.CircuitBreakerConfig
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    /**
     * 失败阈值次数
     * <p>
     * 在时间窗口内，失败次数达到此值则触发熔断。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 失败阈值次数
     */
    int failureRateThreshold() default -1;

    /**
     * 成功阈值次数
     * <p>
     * 半开状态下，连续成功次数达到此值则关闭熔断，恢复正常调用。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 成功阈值次数
     */
    int successRateThreshold() default -1;

    /**
     * 时间窗口大小（毫秒）
     * <p>
     * 统计失败/成功次数的时间范围，超过此时间窗口的数据会被清除。
     * 每次状态切换都会重置统计窗口。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 时间窗口大小（毫秒）
     */
    long windowSizeMillis() default -1;

    /**
     * 熔断持续时间（毫秒）
     * <p>
     * 熔断开启后持续的时间，在此期间所有请求都会被拒绝。
     * 超过此时间后，熔断器会进入半开状态，尝试允许少量请求通过。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 熔断持续时间（毫秒）
     */
    long breakDurationMillis() default -1;

    /**
     * 错误率阈值（百分比）
     * <p>
     * 当时间窗口内的错误率达到此值时触发熔断。
     * 例如：设置为 50 表示错误率达到 50% 时触发熔断。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 错误率阈值（百分比）
     */
    double errorRateThreshold() default -1;

    /**
     * 最小请求数
     * <p>
     * 请求数达到此值才开始计算错误率。
     * 如果请求数过少，错误率的统计结果不具有参考意义。
     * 默认 -1 表示使用配置文件中的全局配置。
     *
     * @return 最小请求数
     */
    int minRequestNumber() default -1;
}