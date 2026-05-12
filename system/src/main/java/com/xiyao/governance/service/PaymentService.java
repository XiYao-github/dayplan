package com.xiyao.governance.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentService {

    /**
     * 核心支付方法 - 组合使用5种治理策略
     *
     * 注意：当使用 @TimeLimiter 时，方法必须返回 CompletableFuture
     */
    @RateLimiter(name = "paymentService", fallbackMethod = "fallback")
    @Bulkhead(name = "paymentService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    @TimeLimiter(name = "paymentService", fallbackMethod = "fallback")
    @Retry(name = "paymentService", fallbackMethod = "fallback")
    public CompletableFuture<String> pay(String orderId, Double amount) {
        log.info("开始处理支付请求，订单号: {}, 金额: {}", orderId, amount);

        return CompletableFuture.supplyAsync(() -> {
            // 模拟调用第三方支付网关
            return callThirdPartyPayment(orderId, amount);
        });
    }

    /**
     * 实际调用第三方支付的方法
     * 模拟各种失败场景
     */
    private String callThirdPartyPayment(String orderId, Double amount) {
        // 模拟随机失败（用于重试测试）
        if (Math.random() < 0.3) {
            log.warn("支付网关网络抖动，订单: {}", orderId);
            throw new RuntimeException("网络连接超时");
        }

        // 模拟慢调用（用于超时测试）
        if (Math.random() < 0.2) {
            try {
                Thread.sleep(5000);  // 5秒 > 3秒超时阈值
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 模拟业务失败
        if (amount > 10000) {
            throw new RuntimeException("金额超过单笔限额");
        }

        return String.format("支付成功！订单: %s, 金额: %.2f", orderId, amount);
    }

    /**
     * 统一降级方法
     *
     * 参数要求（必须遵守）：
     * 1. 与原方法参数一致
     * 2. 最后加一个 Throwable 参数接收异常
     * 3. 返回值类型与原方法一致
     */
    private CompletableFuture<String> fallback(String orderId, Double amount, Throwable t) {
        log.error("支付服务降级触发，订单: {}, 原因: {}", orderId, t.getMessage());

        return CompletableFuture.completedFuture(
                String.format("支付服务繁忙，请稍后重试。订单: %s (原因: %s)", orderId, t.getMessage())
        );
    }
}