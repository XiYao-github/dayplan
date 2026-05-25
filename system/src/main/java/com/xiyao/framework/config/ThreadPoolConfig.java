package com.xiyao.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * <p>
 * 负责配置项目所需的异步任务执行线程池。
 * 采用 Spring 异步机制（@Async）时自动使用此处配置的线程池。
 *
 * <p>
 * <b>线程池设计考量：</b>
 * <ul>
 *     <li>核心线程数不宜过大，避免过度竞争 CPU</li>
 *     <li>队列容量设置合理，防止内存溢出</li>
 *     <li>拒绝策略选择 CallerRunsPolicy，由调用方执行，起到限流作用</li>
 * </ul>
 *
 * @author xiyao
 * @see ThreadPoolTaskExecutor
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 日志异步线程池
     * <p>
     * 用于 @Async 注解的异步日志记录。
     * 采用较小的线程池配置，因为日志操作不应阻塞主业务。
     *
     * <p>
     * <b>配置说明：</b>
     * <ul>
     *     <li>corePoolSize=2：常驻线程数，控制资源占用</li>
     *     <li>maxPoolSize=5：最大线程数，峰值时扩展</li>
     *     <li>queueCapacity=100：缓冲队列大小</li>
     *     <li>CallerRunsPolicy：队列满时由调用方执行，防止任务丢失</li>
     * </ul>
     *
     * @return 异步执行器
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：保持活跃的最小线程数
        executor.setCorePoolSize(2);
        // 最大线程数：峰值时允许创建的最大线程数
        executor.setMaxPoolSize(5);
        // 队列容量：超过核心线程数的任务进入队列等待
        executor.setQueueCapacity(100);
        // 空闲线程存活时间：超过核心线程数的线程空闲多久后回收
        executor.setKeepAliveSeconds(60);
        // 线程名前缀：便于日志追踪和问题排查
        executor.setThreadNamePrefix("log-async-");
        // 拒绝策略：队列满时由调用方执行（同步执行）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
