package com.xiyao.governance.core.circuit;

import com.xiyao.governance.enums.CircuitState;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 熔断器状态
 * <p>
 * 记录熔断器的当前状态和统计数据，用于判断是否应该触发熔断或恢复。
 * 所有统计数据使用原子变量，保证线程安全。
 *
 * <p>
 * <b>状态说明：</b>
 * <ul>
 *     <li>CLOSED：正常状态，统计失败率，达到阈值触发熔断</li>
 *     <li>OPEN：熔断状态，拒绝所有请求，超时后进入 HALF_OPEN</li>
 *     <li>HALF_OPEN：半开状态，允许试探性请求，成功则关闭，失败则重新熔断</li>
 * </ul>
 *
 * <p>
 * <b>统计指标：</b>
 * <ul>
 *     <li>failureCount：失败次数（用于触发熔断）</li>
 *     <li>successCount：成功次数（用于半开状态恢复）</li>
 *     <li>totalRequest：总请求数</li>
 *     <li>failedRequest：失败请求数（用于计算错误率）</li>
 *     <li>lastFailureTime：上次失败时间（可用于滑动窗口）</li>
 *     <li>stateChangedTime：状态切换时间（用于超时判断）</li>
 * </ul>
 *
 * @author xiyao
 * @see CircuitState
 * @see CircuitBreakerManager
 */
@Data
public class CircuitBreakerState {
    /**
     * 当前熔断器状态
     */
    private CircuitState state = CircuitState.CLOSED;

    /**
     * 失败次数计数器
     * <p>
     * 在时间窗口内累计的失败次数，达到阈值触发熔断。
     * 每次状态切换为 CLOSED 时重置。
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * 成功次数计数器
     * <p>
     * 半开状态下累计的成功次数，达到阈值触发恢复。
     * 进入 HALF_OPEN 时重置。
     */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 总请求数计数器
     */
    private final AtomicInteger totalRequest = new AtomicInteger(0);

    /**
     * 失败请求数计数器
     * <p>
     * 用于计算错误率 = failedRequest / totalRequest * 100%
     */
    private final AtomicInteger failedRequest = new AtomicInteger(0);

    /**
     * 上次失败时间（毫秒）
     * <p>
     * 记录最近一次失败的时间戳，可用于滑动窗口等高级特性。
     */
    private final AtomicLong lastFailureTime = new AtomicLong(0);

    /**
     * 状态切换时间（毫秒）
     * <p>
     * 记录最近一次状态切换的时间戳，用于判断熔断超时。
     */
    private final AtomicLong stateChangedTime = new AtomicLong(System.currentTimeMillis());

    /**
     * 时间窗口大小（毫秒）
     */
    private final long windowSizeMillis;

    /**
     * 熔断持续时间（毫秒）
     */
    private final long breakDurationMillis;

    /**
     * 失败次数阈值
     */
    private final int failureRateThreshold;

    /**
     * 成功次数阈值（半开恢复用）
     */
    private final int successRateThreshold;

    /**
     * 错误率阈值（百分比）
     */
    private final double errorRateThreshold;

    /**
     * 最小请求数
     */
    private final int minRequestNumber;

    /**
     * 构造函数
     *
     * @param failureRateThreshold  失败次数阈值
     * @param successRateThreshold  成功次数阈值
     * @param windowSizeMillis      时间窗口大小
     * @param breakDurationMillis   熔断持续时间
     * @param errorRateThreshold    错误率阈值
     * @param minRequestNumber      最小请求数
     */
    public CircuitBreakerState(int failureRateThreshold, int successRateThreshold,
                               long windowSizeMillis, long breakDurationMillis,
                               double errorRateThreshold, int minRequestNumber) {
        this.failureRateThreshold = failureRateThreshold;
        this.successRateThreshold = successRateThreshold;
        this.windowSizeMillis = windowSizeMillis;
        this.breakDurationMillis = breakDurationMillis;
        this.errorRateThreshold = errorRateThreshold;
        this.minRequestNumber = minRequestNumber;
    }

    /**
     * 记录一次失败调用
     * <p>
     * 更新失败计数器和总请求计数器和失败请求计数器。
     * 同时更新最近失败时间。
     */
    public void recordFailure() {
        failureCount.incrementAndGet();
        failedRequest.incrementAndGet();
        totalRequest.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
    }

    /**
     * 记录一次成功调用
     * <p>
     * 更新成功计数器和总请求计数器。
     */
    public void recordSuccess() {
        totalRequest.incrementAndGet();
        successCount.incrementAndGet();
    }

    /**
     * 重置时间窗口内的统计
     * <p>
     * 当状态切换为 CLOSED 或时间窗口到期时调用。
     * 清除失败次数、成功次数、总请求数、失败请求数。
     */
    public void resetWindow() {
        failureCount.set(0);
        successCount.set(0);
        totalRequest.set(0);
        failedRequest.set(0);
    }

    /**
     * 判断是否达到熔断条件
     * <p>
     * 检查两个维度：
     * <ol>
     *     <li>失败次数是否达到阈值</li>
     *     <li>错误率是否达到阈值（需满足最小请求数）</li>
     * </ol>
     * 满足任一条件即返回 true。
     *
     * @return true 表示应该触发熔断
     */
    public boolean shouldTrip() {
        int total = totalRequest.get();

        // 请求数少于最小阈值，不计算错误率
        if (total < minRequestNumber) {
            return false;
        }

        // 基于失败次数判断
        if (failureCount.get() >= failureRateThreshold) {
            return true;
        }

        // 基于错误率判断
        double errorRate = (double) failedRequest.get() / total * 100;
        return errorRate >= errorRateThreshold;
    }

    /**
     * 判断是否达到恢复条件
     * <p>
     * 在半开状态下使用，检查成功次数是否达到阈值。
     *
     * @return true 表示应该关闭熔断，恢复正常调用
     */
    public boolean shouldAllowRequest() {
        return successCount.get() >= successRateThreshold;
    }

    /**
     * 判断熔断超时是否可以进入半开状态
     * <p>
     * 检查从 OPEN 状态开始到现在是否已经超过了熔断持续时间。
     *
     * @return true 表示可以进入半开状态
     */
    public boolean isBreakTimeout() {
        return System.currentTimeMillis() - stateChangedTime.get() >= breakDurationMillis;
    }

    /**
     * 切换熔断器状态
     * <p>
     * 执行状态转换，并更新状态切换时间。
     * 进入 CLOSED 状态时重置统计窗口。
     * 进入 HALF_OPEN 状态时重置成功计数。
     *
     * @param newState 要切换到的目标状态
     */
    public void transitionTo(CircuitState newState) {
        this.state = newState;
        this.stateChangedTime.set(System.currentTimeMillis());

        if (newState == CircuitState.CLOSED) {
            // 恢复正常，重置统计窗口
            resetWindow();
        } else if (newState == CircuitState.HALF_OPEN) {
            // 进入半开，重置成功计数
            successCount.set(0);
        }
    }
}