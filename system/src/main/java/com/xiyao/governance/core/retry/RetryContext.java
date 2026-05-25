package com.xiyao.governance.core.retry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 重试上下文
 * <p>
 * 记录重试过程中的状态信息，用于跟踪重试次数和判断是否应该继续重试。
 * 所有状态使用原子变量保证线程安全。
 *
 * <p>
 * <b>主要属性：</b>
 * <ul>
 *     <li>attemptCount：当前尝试次数（包括首次执行）</li>
 *     <li>startTimeMillis：重试开始时间</li>
 *     <li>maxAttempts：最大尝试次数</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * RetryContext context = new RetryContext(3);
 * while (!context.isExhausted()) {
 *     try {
 *         return doSomething();
 *     } catch (Exception e) {
 *         int attempt = context.incrementAndGet();
 *         if (context.isExhausted()) {
 *             throw e;
 *         }
 *         Thread.sleep(calculateDelay(attempt));
 *     }
 * }
 * }</pre>
 *
 * @author xiyao
 * @see RetryAspect
 * @see com.xiyao.governance.annotation.Retryable
 */
public class RetryContext {

    /**
     * 尝试次数计数器
     * <p>
     * 原子整数，保证线程安全。
     * 每次调用 incrementAndGet() 后递增。
     */
    private final AtomicInteger attemptCount = new AtomicInteger(0);

    /**
     * 重试开始时间（毫秒）
     * <p>
     * 用于计算已用时间，可用于超时控制等场景。
     */
    private final long startTimeMillis;

    /**
     * 最大尝试次数
     * <p>
     * 包含首次执行在内的最大执行次数。
     * 例如：设置为 3 表示最多执行 3 次（第 1 次 + 2 次重试）。
     */
    private final int maxAttempts;

    /**
     * 构造函数
     *
     * @param maxAttempts 最大尝试次数
     */
    public RetryContext(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.startTimeMillis = System.currentTimeMillis();
    }

    /**
     * 原子递增并返回当前尝试次数
     * <p>
     * 每次重试前调用，用于获取当前是第几次尝试。
     *
     * @return 当前尝试次数（从 1 开始）
     */
    public int incrementAndGet() {
        return attemptCount.incrementAndGet();
    }

    /**
     * 获取当前尝试次数
     *
     * @return 当前尝试次数
     */
    public int getAttemptCount() {
        return attemptCount.get();
    }

    /**
     * 判断重试次数是否已用尽
     * <p>
     * 检查当前尝试次数是否达到最大次数。
     * 当 attemptCount >= maxAttempts 时返回 true。
     *
     * @return true 表示已用尽，不应继续重试
     */
    public boolean isExhausted() {
        return attemptCount.get() >= maxAttempts;
    }

    /**
     * 获取已用时间（毫秒）
     * <p>
     * 计算从重试开始到现在经过的时间。
     * 可用于实现超时重试等高级特性。
     *
     * @return 已用时间（毫秒）
     */
    public long getElapsedTimeMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }

    /**
     * 获取最大尝试次数
     *
     * @return 最大尝试次数
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }
}