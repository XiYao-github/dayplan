package com.xiyao.framework.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 通用异步任务线程池
     * 用于处理 @Async 注解标注的方法，如异步日志记录、异步短信发送等。
     *
     * @return 线程池执行器
     */
    @Bean("asyncExecutor")
    @Primary
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：CPU 核心数 * 2
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // 最大线程数：核心线程数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("async-exec-");
        // 拒绝策略：由调用线程执行（保证任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        log.info("通用异步线程池初始化完成，核心线程数：{}，最大线程数：{}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }

    /**
     * 日志专用线程池
     * 用于异步写入操作日志、登录日志，避免日志写入影响主业务性能。
     *
     * @return 日志线程池执行器
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("log-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("日志线程池初始化完成，核心线程数：{}，最大线程数：{}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }

    /**
     * 导出任务专用线程池
     * 用于大数据量导出（Excel/PDF），独立线程池避免与其他任务竞争资源。
     *
     * @return 导出线程池执行器
     */
    @Bean("exportExecutor")
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("export-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("导出线程池初始化完成，核心线程数：{}，最大线程数：{}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }
}
