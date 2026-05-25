package com.xiyao.governance.core.ratelimit;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流器
 * <p>
 * 使用令牌桶算法实现精确的流量控制。
 * 支持突发流量，同时保证长期平均速率不超过限制。
 *
 * <p>
 * <b>算法原理：</b>
 * <ul>
 *     <li>以固定速率向桶中添加令牌（每秒 permitsPerSecond 个）</li>
 *     <li>桶有最大容量限制（maxBurstRequests），超出则丢弃</li>
 *     <li>每次请求从桶中获取一个令牌</li>
 *     <li>桶空时请求被拒绝</li>
 * </ul>
 *
 * <p>
 * <b>关键特性：</b>
 * <ul>
 *     <li>允许一定程度的突发流量（最多 maxBurstRequests 个）</li>
 *     <li>使用原子操作保证线程安全</li>
 *     <li>后台线程定时补充令牌</li>
 *     <li>支持多 key 隔离，每个 key 有独立的令牌桶</li>
 * </ul>
 *
 * <p>
 * <b>令牌补充逻辑：</b>
 * <pre>
 * 每秒补充 tokens = elapsed * permitsPerSecond / 1000
 * 新令牌数 = min(maxBurstRequests, 当前令牌 + tokens)
 * </pre>
 *
 * @author xiyao
 * @see RateLimitAspect
 * @see com.xiyao.governance.annotation.RateLimit
 */
public class RateLimiter {
    /**
     * 每秒允许的令牌数（令牌补充速率）
     */
    private final double permitsPerSecond;

    /**
     * 令牌桶最大容量（突发容量）
     */
    private final int maxBurstRequests;

    /**
     * 当前存储的令牌数
     */
    private final AtomicLong storedTokens;

    /**
     * 上次令牌补充时间（毫秒）
     */
    private final AtomicLong lastUpdateTime;

    /**
     * 令牌补充调度器
     * <p>
     * 单线程调度器，定时执行令牌补充任务。
     * 使用守护线程，不阻止 JVM 退出。
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 多 key 限流器映射
     * <p>
     * 支持对不同资源独立限流。
     * Key: 资源标识（如方法签名）
     * Value: 该资源的限流器
     */
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param permitsPerSecond 每秒令牌数（补充速率）
     * @param maxBurstRequests  令牌桶最大容量（突发容量）
     */
    public RateLimiter(double permitsPerSecond, int maxBurstRequests) {
        this.permitsPerSecond = permitsPerSecond;
        this.maxBurstRequests = maxBurstRequests;
        this.storedTokens = new AtomicLong(maxBurstRequests);
        this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "governance-rate-limiter-scheduler");
            t.setDaemon(true);  // 守护线程，不阻止 JVM 退出
            return t;
        });
        // 启动定时任务，每秒补充令牌
        scheduler.scheduleAtFixedRate(this::refillTokens, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取一个令牌
     * <p>
     * 从令牌桶中获取一个令牌，成功返回 true，桶空返回 false。
     * 每次调用前会先补充令牌。
     *
     * @return true 获取成功，false 限流
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量的令牌
     * <p>
     * 原子操作：检查令牌数足够后，扣减令牌。
     * 使用 CAS（compareAndSet）保证线程安全，避免竞争条件。
     *
     * @param permits 请求的令牌数
     * @return true 获取成功，false 令牌不足
     */
    public boolean tryAcquire(int permits) {
        // 先补充令牌
        refillTokens();

        // 自旋CAS直到成功或失败
        long current;
        long newValue;
        do {
            current = storedTokens.get();
            if (current < permits) {
                // 令牌不足，限流
                return false;
            }
            newValue = current - permits;
        } while (!storedTokens.compareAndSet(current, newValue));
        return true;
    }

    /**
     * 补充令牌
     * <p>
     * 根据 elapsed 时间计算应该补充的令牌数。
     * 使用原子操作保证线程安全。
     * 令牌数不会超过最大容量。
     */
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long last = lastUpdateTime.get();
        long elapsed = now - last;

        if (elapsed > 0) {
            // 计算应该补充的令牌数
            // permitsPerSecond 是每秒补充的令牌数，所以 elapsed/1000 是秒数
            long tokensToAdd = (long) (elapsed * permitsPerSecond / 1000.0);
            if (tokensToAdd > 0) {
                long current = storedTokens.get();
                long newValue = Math.min(maxBurstRequests, current + tokensToAdd);
                // CAS 保证线程安全
                if (storedTokens.compareAndSet(current, newValue)) {
                    lastUpdateTime.set(now);
                }
            }
        }
    }

    /**
     * 获取当前可用令牌数
     * <p>
     * 用于监控和调试。
     * 注意：返回值可能不准确，因为补充和获取可能同时进行。
     *
     * @return 当前可用令牌数
     */
    public long getAvailablePermits() {
        return storedTokens.get();
    }

    /**
     * 关闭限流器
     * <p>
     * 停止令牌补充调度器，释放资源。
     * 通常在应用关闭时调用。
     */
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * 根据 key 获取或创建限流器
     * <p>
     * 每个 key 对应独立的令牌桶，实现资源隔离限流。
     * 例如：对不同接口设置不同的限流阈值。
     *
     * @param key               资源标识
     * @param permitsPerSecond  每秒令牌数
     * @param maxBurstRequests  令牌桶最大容量
     * @return 对应的限流器
     */
    public RateLimiter getOrCreate(String key, double permitsPerSecond, int maxBurstRequests) {
        return limiters.computeIfAbsent(key, k -> new RateLimiter(permitsPerSecond, maxBurstRequests));
    }
}