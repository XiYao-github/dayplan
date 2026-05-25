package com.xiyao.governance.core.bulkhead;

import java.util.concurrent.*;

/**
 * 舱壁管理器
 * <p>
 * 负责管理多个隔离线程池，实现舱壁模式（Bulkhead Pattern）。
 * 每个隔离名称对应一个独立的 ThreadPoolExecutor，实现资源隔离。
 *
 * <p>
 * <b>设计原理：</b>
 * <ul>
 *     <li>使用 ConcurrentHashMap 存储线程池，保证线程安全</li>
 *     <li>线程池懒创建，首次使用时创建</li>
 *     <li>使用 Daemon 线程，不阻止 JVM 退出</li>
 *     <li>CallerRunsPolicy 拒绝策略，将任务回退给调用者</li>
 * </ul>
 *
 * <p>
 * <b>线程池配置策略：</b>
 * <ul>
 *     <li>核心线程数：保持最小工作线程</li>
 *     <li>最大线程数：允许扩展的最大工作线程</li>
 *     <li>队列容量：等待执行的任务队列大小</li>
 *     <li>KeepAlive：60秒回收多余空闲线程</li>
 * </ul>
 *
 * @author xiyao
 * @see BulkheadAspect
 * @see java.util.concurrent.ThreadPoolExecutor
 */
public class BulkheadManager {

    /**
     * 隔离线程池映射表
     * Key: 隔离名称（通常为方法签名）
     * Value: 对应的线程池执行器
     */
    private final ConcurrentHashMap<String, ThreadPoolExecutor> executors = new ConcurrentHashMap<>();

    /**
     * 默认核心线程数
     */
    private final int defaultCoreSize;

    /**
     * 默认最大线程数
     */
    private final int defaultMaxSize;

    /**
     * 默认队列容量
     */
    private final int defaultQueueCapacity;

    /**
     * 构造函数
     *
     * @param defaultCoreSize    默认核心线程数
     * @param defaultMaxSize     默认最大线程数
     * @param defaultQueueCapacity 默认队列容量
     */
    public BulkheadManager(int defaultCoreSize, int defaultMaxSize, int defaultQueueCapacity) {
        this.defaultCoreSize = defaultCoreSize;
        this.defaultMaxSize = defaultMaxSize;
        this.defaultQueueCapacity = defaultQueueCapacity;
    }

    /**
     * 获取或创建隔离线程池
     * <p>
     * 如果指定名称的线程池已存在，直接返回；
     * 否则创建新的线程池并注册。
     * 使用 computeIfAbsent 保证线程安全，避免重复创建。
     *
     * @param name          隔离名称（唯一标识）
     * @param coreSize      核心线程数
     * @param maxSize       最大线程数
     * @param queueCapacity 队列容量
     * @return 对应的线程池执行器
     */
    public ThreadPoolExecutor getOrCreate(String name, int coreSize, int maxSize, int queueCapacity) {
        return executors.computeIfAbsent(name, k -> createExecutor(coreSize, maxSize, queueCapacity));
    }

    /**
     * 创建线程池
     * <p>
     * 配置说明：
     * <ul>
     *     <li>corePoolSize：核心线程数，线程池保持的最小线程数</li>
     *     <li>maximumPoolSize：最大线程数，允许的最大工作线程数</li>
     *     <li>keepAliveTime：60秒，多余空闲线程的存活时间</li>
     *     <li>workQueue：LinkedBlockingQueue，任务队列</li>
     *     <li>threadFactory：自定义线程工厂，创建 Daemon 线程</li>
     *     <li>handler：CallerRunsPolicy，队列满时由调用者线程执行</li>
     * </ul>
     *
     * @param coreSize      核心线程数
     * @param maxSize       最大线程数
     * @param queueCapacity 队列容量
     * @return 配置好的 ThreadPoolExecutor
     */
    private ThreadPoolExecutor createExecutor(int coreSize, int maxSize, int queueCapacity) {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                // 自定义线程工厂，创建 Daemon 线程避免阻止 JVM 退出
                r -> {
                    Thread t = new Thread(r, "governance-bulkhead-" + Thread.currentThread().getName());
                    t.setDaemon(true);
                    return t;
                },
                // CallerRunsPolicy：队列满时由调用者线程执行，提供背压机制
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 提交任务执行（带返回结果）
     * <p>
     * 适用于需要获取执行结果的场景。
     *
     * @param name  隔离名称
     * @param task  要执行的任务
     * @param <T>   返回值类型
     * @return Future 对象，可获取执行结果或取消任务
     * @throws IllegalStateException 如果隔离器未找到
     */
    public <T> Future<T> submit(String name, Callable<T> task) {
        ThreadPoolExecutor executor = executors.get(name);
        if (executor == null) {
            throw new IllegalStateException("隔离器未找到: " + name);
        }
        return executor.submit(task);
    }

    /**
     * 执行任务（无返回）
     *
     * @param name 隔离名称
     * @param task 要执行的任务
     * @throws IllegalStateException 如果隔离器未找到
     */
    public void execute(String name, Runnable task) {
        ThreadPoolExecutor executor = executors.get(name);
        if (executor == null) {
            throw new IllegalStateException("隔离器未找到: " + name);
        }
        executor.execute(task);
    }

    /**
     * 获取活跃线程数
     * <p>
     * 用于监控和调试，帮助了解线程池的使用情况。
     *
     * @param name 隔离名称
     * @return 当前活跃的线程数
     */
    public int getActiveCount(String name) {
        ThreadPoolExecutor executor = executors.get(name);
        return executor != null ? executor.getActiveCount() : 0;
    }

    /**
     * 获取队列等待数
     * <p>
     * 用于监控和调试，了解当前有多少任务在排队等待。
     *
     * @param name 隔离名称
     * @return 队列中等待的任务数
     */
    public int getQueueSize(String name) {
        ThreadPoolExecutor executor = executors.get(name);
        return executor != null ? executor.getQueue().size() : 0;
    }

    /**
     * 关闭所有线程池
     * <p>
     * 遍历所有隔离线程池，尝试优雅关闭。
     * 超时后强制关闭，确保资源释放。
     * 通常在应用关闭时调用。
     */
    public void shutdown() {
        executors.values().forEach(executor -> {
            executor.shutdown();
            try {
                // 等待最多 5 秒让任务完成
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // 超时未完成，强制关闭
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // 等待被中断，强制关闭
                executor.shutdownNow();
                // 恢复中断状态
                Thread.currentThread().interrupt();
            }
        });
        // 清空映射表
        executors.clear();
    }
}