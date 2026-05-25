package com.xiyao.governance.core.circuit;

import com.xiyao.governance.enums.CircuitState;

import java.util.concurrent.*;

/**
 * 熔断器管理器
 * <p>
 * 负责管理多个方法的熔断器状态，提供熔断器的创建、查询、状态更新等功能。
 * 使用 ConcurrentHashMap 保证线程安全，支持高并发访问。
 *
 * <p>
 * <b>主要功能：</b>
 * <ul>
 *     <li>获取或创建熔断器：根据方法 key 创建或获取已有的熔断器</li>
 *     <li>判断是否允许请求：根据熔断器当前状态判断是否放行</li>
 *     <li>记录成功/失败：更新熔断器统计信息</li>
 *     <li>状态转换：根据统计结果触发熔断或恢复</li>
 * </ul>
 *
 * <p>
 * <b>状态判断逻辑：</b>
 * <ul>
 *     <li>CLOSED（关闭）：正常统计，允许请求，时间窗口到期重置统计</li>
 *     <li>OPEN（打开）：拒绝所有请求，超时后进入 HALF_OPEN</li>
 *     <li>HALF_OPEN（半开）：允许试探性请求，根据结果决定恢复或再次熔断</li>
 * </ul>
 *
 * @author xiyao
 * @see CircuitBreakerState
 * @see CircuitState
 * @see CircuitBreakerAspect
 */
public class CircuitBreakerManager {

    /**
     * 熔断器状态映射表
     * Key: 方法唯一标识（类名.方法名）
     * Value: 熔断器状态对象
     */
    private final ConcurrentHashMap<String, CircuitBreakerState> breakers = new ConcurrentHashMap<>();

    /**
     * 获取或创建熔断器
     * <p>
     * 如果指定 key 的熔断器已存在，直接返回；
     * 否则创建新的熔断器状态并返回。
     * 使用 computeIfAbsent 保证线程安全，避免重复创建。
     *
     * @param key                  方法唯一标识
     * @param failureRateThreshold 失败次数阈值
     * @param successRateThreshold 成功次数阈值（半开状态恢复用）
     * @param windowSizeMillis     时间窗口大小
     * @param breakDurationMillis  熔断持续时间
     * @param errorRateThreshold   错误率阈值
     * @param minRequestNumber     最小请求数
     * @return 熔断器状态对象
     */
    public CircuitBreakerState getOrCreate(String key, int failureRateThreshold, int successRateThreshold,
                                          long windowSizeMillis, long breakDurationMillis,
                                          double errorRateThreshold, int minRequestNumber) {
        return breakers.computeIfAbsent(key, k -> new CircuitBreakerState(
                failureRateThreshold, successRateThreshold,
                windowSizeMillis, breakDurationMillis,
                errorRateThreshold, minRequestNumber
        ));
    }

    /**
     * 获取熔断器当前状态
     * <p>
     * 如果熔断器不存在，返回 CLOSED（默认状态）。
     *
     * @param key 方法唯一标识
     * @return 熔断器当前状态
     */
    public CircuitState getState(String key) {
        CircuitBreakerState state = breakers.get(key);
        return state != null ? state.getState() : CircuitState.CLOSED;
    }

    /**
     * 判断是否允许请求
     * <p>
     * 根据熔断器当前状态判断是否放行请求：
     * <ul>
     *     <li>CLOSED：允许请求，检查时间窗口是否需要重置</li>
     *     <li>OPEN：检查超时是否到期，到期则转入 HALF_OPEN 并允许请求</li>
     *     <li>HALF_OPEN：允许请求（试探性）</li>
     * </ul>
     *
     * @param key 方法唯一标识
     * @return true 表示允许请求，false 表示拒绝
     */
    public boolean isCallPermitted(String key) {
        CircuitBreakerState state = breakers.get(key);
        if (state == null) {
            // 熔断器不存在，默认允许请求
            return true;
        }

        CircuitState currentState = state.getState();
        if (currentState == CircuitState.CLOSED) {
            // CLOSED 状态：检查是否需要重置时间窗口
            if (System.currentTimeMillis() - state.getStateChangedTime().get() >= state.getWindowSizeMillis()) {
                state.resetWindow();  // 重置统计，进入新的时间窗口
            }
            return true;
        } else if (currentState == CircuitState.OPEN) {
            // OPEN 状态：检查是否超时可以进入半开
            if (state.isBreakTimeout()) {
                state.transitionTo(CircuitState.HALF_OPEN);
                return true;
            }
            return false;
        } else if (currentState == CircuitState.HALF_OPEN) {
            // HALF_OPEN 状态：允许试探性请求
            return true;
        }
        return true;  // 兜底，默认允许
    }

    /**
     * 记录成功调用
     * <p>
     * 更新熔断器的成功计数。
     * 如果当前是 HALF_OPEN 状态，检查是否达到恢复条件。
     *
     * @param key 方法唯一标识
     */
    public void recordSuccess(String key) {
        CircuitBreakerState state = breakers.get(key);
        if (state == null) {
            return;
        }

        // 记录成功
        state.recordSuccess();

        // 如果是半开状态，检查是否达到恢复条件
        if (state.getState() == CircuitState.HALF_OPEN) {
            if (state.shouldAllowRequest()) {
                state.transitionTo(CircuitState.CLOSED);
            }
        }
    }

    /**
     * 记录失败调用
     * <p>
     * 更新熔断器的失败计数。
     * 如果当前是 CLOSED 状态，检查是否达到熔断条件。
     * 如果当前是 HALF_OPEN 状态，失败会立即触发熔断。
     *
     * @param key 方法唯一标识
     */
    public void recordFailure(String key) {
        CircuitBreakerState state = breakers.get(key);
        if (state == null) {
            return;
        }

        // 记录失败
        state.recordFailure();

        CircuitState currentState = state.getState();
        if (currentState == CircuitState.CLOSED) {
            // 检查是否需要触发熔断
            if (state.shouldTrip()) {
                state.transitionTo(CircuitState.OPEN);
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            // 半开状态下失败，立即进入熔断
            state.transitionTo(CircuitState.OPEN);
        }
    }

    /**
     * 清除熔断器状态
     * <p>
     * 通常用于测试或动态配置变更时重置熔断器。
     *
     * @param key 方法唯一标识
     */
    public void clear(String key) {
        breakers.remove(key);
    }
}