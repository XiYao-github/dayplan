package com.xiyao.governance.enums;

/**
 * 熔断器状态枚举
 * <p>
 * 定义熔断器的三种状态及其转换关系。
 *
 * <p>
 * <b>状态说明：</b>
 * <ul>
 *     <li>{@link #CLOSED}：关闭状态，正常工作，统计失败率</li>
 *     <li>{@link #OPEN}：打开状态，熔断中，拒绝所有请求</li>
 *     <li>{@link #HALF_OPEN}：半开状态，试探性恢复，允许部分请求</li>
 * </ul>
 *
 * <p>
 * <b>状态转换图：</b>
 * <pre>
 *     CLOSED ──(失败率达标)──&gt; OPEN
 *       ↑                       │
 *       │                       ↓
 *       └──(超时后)──&gt; HALF_OPEN ──(成功率达标)──&gt; CLOSED
 *                           │
 *                           └────(失败)────────&gt; OPEN
 * </pre>
 *
 * <p>
 * <b>各状态行为：</b>
 * <ul>
 *     <li>CLOSED：所有请求正常通过，统计失败/成功次数</li>
 *     <li>OPEN：所有请求直接拒绝，抛出异常</li>
 *     <li>HALF_OPEN：允许部分请求通过，根据结果决定恢复到 CLOSED 或回到 OPEN</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.circuit.CircuitBreakerState
 * @see com.xiyao.governance.core.circuit.CircuitBreakerManager
 * @see com.xiyao.governance.annotation.CircuitBreaker
 */
public enum CircuitState {

    /**
     * 关闭状态（正常工作）
     * <p>
     * 熔断器处于正常状态，统计调用结果。
     * 当失败率达到阈值时，状态转换为 OPEN。
     * <p>
     * 特点：
     * <ul>
     *     <li>所有请求正常通过</li>
     *     <li>统计失败次数和成功次数</li>
     *     <li>时间窗口到期时重置统计</li>
     * </ul>
     */
    CLOSED,

    /**
     * 打开状态（熔断中）
     * <p>
     * 熔断器处于熔断状态，所有请求直接失败。
     * 等待 breakDurationMillis 时间后，状态转换为 HALF_OPEN。
     * <p>
     * 特点：
     * <ul>
     *     <li>所有请求直接抛出异常</li>
     *     <li>不执行实际业务逻辑</li>
     *     <li>给下游服务恢复时间</li>
     * </ul>
     */
    OPEN,

    /**
     * 半开状态（试探性恢复）
     * <p>
     * 熔断器处于恢复试探状态，允许部分请求通过。
     * 成功率达到阈值时，状态转换为 CLOSED；
     * 失败则回到 OPEN。
     * <p>
     * 特点：
     * <ul>
     *     <li>允许部分请求通过（试探性）</li>
     *     <li>重置成功计数</li>
     *     <li>失败立即回到 OPEN，防止雪崩</li>
     * </ul>
     */
    HALF_OPEN
}