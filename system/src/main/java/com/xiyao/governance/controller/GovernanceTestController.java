package com.xiyao.governance.controller;

import com.xiyao.common.utils.Result;
import com.xiyao.governance.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * 治理功能测试控制器
 * <p>
 * 提供限流、熔断、隔离、重试、降级等治理功能的接口测试。
 * 用于在开发/测试环境验证治理配置是否生效。
 *
 * <p>
 * <b>测试接口列表：</b>
 * <ul>
 *     <li>限流测试：/governance/test/ratelimit/*</li>
 *     <li>熔断测试：/governance/test/circuit/*</li>
 *     <li>降级测试：/governance/test/fallback/*</li>
 *     <li>隔离测试：/governance/test/bulkhead/*</li>
 *     <li>重试测试：/governance/test/retry/*</li>
 *     <li>组合测试：/governance/test/combo/*</li>
 * </ul>
 *
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>此控制器仅用于测试，生产环境应移除或禁用</li>
 *     <li>部分接口会随机抛出异常以模拟故障场景</li>
 *     <li>建议使用 Jmeter 或 wrk 等工具进行压测，验证限流/熔断效果</li>
 * </ul>
 *
 * @author xiyao
 * @see RateLimit
 * @see CircuitBreaker
 * @see Fallback
 * @see Bulkhead
 * @see Retryable
 */
@RestController
@RequestMapping("/governance/test")
public class GovernanceTestController {

    /**
     * 随机数生成器，用于模拟随机失败场景
     */
    private static final Random RANDOM = new Random();

    // ==================== 限流测试接口 ====================

    /**
     * 限流测试（全局配置）
     * <p>
     * 使用 @RateLimit 默认配置测试限流功能。
     * 无自定义参数时，会使用 governance.rate-limit.* 中的全局配置。
     *
     * @return 限流测试成功响应
     */
    @RateLimit
    @GetMapping("/ratelimit/global")
    public Result rateLimitGlobal() {
        return Result.ok("限流测试（全局配置）成功");
    }

    /**
     * 限流测试（自定义配置）
     * <p>
     * 使用自定义参数测试限流功能，每秒仅允许 1 个请求，突发容量为 3。
     *
     * @return 限流测试成功响应
     */
    @RateLimit(permitsPerSecond = 1, maxBurstRequests = 3, message = "自定义限流：请求太频繁了")
    @GetMapping("/ratelimit/custom")
    public Result rateLimitCustom() {
        return Result.ok("限流测试（自定义配置）成功");
    }

    // ==================== 熔断测试接口 ====================

    /**
     * 熔断测试（成功场景）
     * <p>
     * 正常返回，验证熔断器在正常情况下不触发。
     *
     * @return 熔断测试成功响应
     */
    @CircuitBreaker
    @GetMapping("/circuit/success")
    public Result circuitSuccess() {
        return Result.ok("熔断测试成功");
    }

    /**
     * 熔断测试（失败场景）
     * <p>
     * 随机抛出 RuntimeException，模拟业务异常。
     * 失败率约 70%，用于测试熔断器是否正常工作。
     *
     * @return 熔断测试成功响应（正常情况下）
     * @throws RuntimeException 模拟业务异常（约 70% 概率）
     */
    @CircuitBreaker(failureRateThreshold = 3, windowSizeMillis = 30000, breakDurationMillis = 10000)
    @GetMapping("/circuit/fail")
    public Result circuitFail() {
        int num = RANDOM.nextInt(10);
        // 70% 概率抛出异常，触发熔断
        if (num < 7) {
            throw new RuntimeException("模拟业务异常");
        }
        return Result.ok("熔断测试成功");
    }

    // ==================== 降级测试接口 ====================

    /**
     * 降级测试（成功场景）
     * <p>
     * 正常返回，不会触发降级。
     *
     * @return 降级测试成功响应
     */
    @Fallback(fallbackClass = TestFallback.class, value = {RuntimeException.class})
    @GetMapping("/fallback/success")
    public Result fallbackSuccess() {
        return Result.ok("降级测试成功");
    }

    /**
     * 降级测试（触发场景）
     * <p>
     * 强制抛出 RuntimeException，触发降级逻辑。
     * 异常会被 FallbackAspect 拦截，调用 TestFallback.fallback() 方法返回降级响应。
     *
     * @return 降级响应（经过降级处理后的结果）
     * @throws RuntimeException 模拟业务异常，用于触发降级
     */
    @Fallback(fallbackClass = TestFallback.class, value = {RuntimeException.class})
    @GetMapping("/fallback/trigger")
    public Result fallbackTrigger() {
        throw new RuntimeException("模拟业务异常，触发降级");
    }

    // ==================== 隔离测试接口 ====================

    /**
     * 隔离测试（成功场景）
     * <p>
     * 正常执行，验证 BulkheadAspect 正常工作。
     *
     * @return 隔离测试成功响应
     */
    @Bulkhead
    @GetMapping("/bulkhead/success")
    public Result bulkheadSuccess() {
        return Result.ok("隔离测试成功");
    }

    /**
     * 隔离测试（自定义配置）
     * <p>
     * 使用自定义线程池配置测试隔离功能。
     *
     * @return 隔离测试成功响应
     */
    @Bulkhead(coreSize = 2, maxSize = 4, queueCapacity = 10, name = "test-bulkhead")
    @GetMapping("/bulkhead/custom")
    public Result bulkheadCustom() {
        return Result.ok("隔离测试（自定义配置）成功");
    }

    // ==================== 重试测试接口 ====================

    /**
     * 重试测试（成功场景）
     * <p>
     * 正常返回，不会触发重试。
     *
     * @return 重试测试成功响应
     */
    @Retryable
    @GetMapping("/retry/success")
    public Result retrySuccess() {
        return Result.ok("重试测试成功");
    }

    /**
     * 重试测试（可重试异常场景）
     * <p>
     * 随机抛出 RetryableException，约 80% 概率触发重试。
     * 使用自定义重试参数：最大 3 次尝试，间隔 500ms。
     *
     * @return 重试测试成功响应（经过重试后）
     * @throws RetryableException 模拟可重试异常（约 80% 概率）
     */
    @Retryable(maxAttempts = 3, intervalMillis = 500, includes = {RetryableException.class})
    @GetMapping("/retry/retryable")
    public Result retryable() {
        int num = RANDOM.nextInt(10);
        // 80% 概率抛出可重试异常
        if (num < 8) {
            throw new RetryableException("模拟可重试异常");
        }
        return Result.ok("重试测试成功");
    }

    /**
     * 重试测试（不可重试异常场景）
     * <p>
     * 抛出 IllegalArgumentException（不在 includes 列表中），不会重试，直接抛出。
     *
     * @return 此处不会返回
     * @throws IllegalArgumentException 不可重试异常，不会触发重试
     */
    @Retryable(maxAttempts = 3, excludes = {RetryableException.class})
    @GetMapping("/retry/non-retryable")
    public Result nonRetryable() {
        throw new IllegalArgumentException("模拟不可重试异常");
    }

    // ==================== 组合测试接口 ====================

    /**
     * 熔断 + 降级组合测试
     * <p>
     * 测试熔断触发后，是否能正确降级。
     *
     * @return 组合测试成功响应（正常情况下）
     * @throws RuntimeException 模拟业务异常，可能触发熔断和降级
     */
    @CircuitBreaker(failureRateThreshold = 2, breakDurationMillis = 5000)
    @Fallback(fallbackClass = TestFallback.class, value = {RuntimeException.class})
    @GetMapping("/combo/circuit-fallback")
    public Result circuitFallbackCombo() {
        int num = RANDOM.nextInt(10);
        // 60% 概率抛出异常
        if (num < 6) {
            throw new RuntimeException("模拟业务异常");
        }
        return Result.ok("熔断+降级组合测试成功");
    }

    /**
     * 重试 + 降级组合测试
     * <p>
     * 测试重试耗尽后，是否能正确降级。
     *
     * @return 组合测试成功响应（经过重试和降级后）
     * @throws RetryableException 模拟可重试异常
     */
    @Retryable(maxAttempts = 2, intervalMillis = 300)
    @Fallback(fallbackClass = TestFallback.class)
    @GetMapping("/combo/retry-fallback")
    public Result retryFallbackCombo() {
        int num = RANDOM.nextInt(10);
        // 70% 概率抛出异常
        if (num < 7) {
            throw new RetryableException("模拟可重试异常");
        }
        return Result.ok("重试+降级组合测试成功");
    }

    // ==================== 内部类定义 ====================

    /**
     * 可重试异常定义
     * <p>
     * 用于测试重试功能，继承 RuntimeException 表示业务级别的可重试异常。
     * 在 @Retryable 的 includes 属性中指定此异常类型，才会触发重试。
     */
    public static class RetryableException extends RuntimeException {
        public RetryableException(String message) {
            super(message);
        }
    }

    /**
     * 降级处理类
     * <p>
     * 提供降级方法，当主业务逻辑失败时被调用。
     * fallback 方法接收原始异常作为参数，可以记录日志或返回友好响应。
     */
    public static class TestFallback {

        /**
         * 降级处理方法
         * <p>
         * 当主业务抛出异常时，返回降级响应。
         *
         * @param e 原始异常对象，包含异常类型和消息
         * @return 降级后的响应结果
         */
        public Result fallback(Throwable e) {
            return Result.ok("【降级响应】服务繁忙，返回降级结果，原始异常：" + e.getClass().getSimpleName());
        }
    }
}