package com.xiyao.governance.core.retry;

import com.xiyao.governance.annotation.Retryable;
import com.xiyao.governance.config.GovernanceProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 重试切面
 * <p>
 * 当方法调用失败时，根据配置的重试策略自动进行重试。
 * 支持固定间隔和指数退避两种重试策略。
 * 通过 AOP 拦截带有 @Retryable 注解的方法，实现重试逻辑。
 *
 * <p>
 * <b>重试策略：</b>
 * <ul>
 *     <li><b>固定间隔</b>：multiplier=1.0 或不设置，每次重试等待相同时间</li>
 *     <li><b>指数退避</b>：multiplier>1.0，等待时间按倍数增长</li>
 * </ul>
 *
 * <p>
 * <b>指数退避计算：</b>
 * <pre>
 * 第 1 次重试：intervalMillis * multiplier^(1-1) = intervalMillis
 * 第 2 次重试：intervalMillis * multiplier^(2-1) = intervalMillis * multiplier
 * 第 3 次重试：intervalMillis * multiplier^(3-1) = intervalMillis * multiplier^2
 * ...
 * </pre>
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>获取重试配置（次数、间隔、倍数）</li>
 *     <li>循环执行目标方法</li>
 *     <li>成功则返回，失败则检查是否应该重试</li>
 *     <li>计算等待时间，sleep 后继续重试</li>
 *     <li>重试次数用尽仍失败，则抛出异常</li>
 * </ol>
 *
 * <p>
 * <b>异常过滤：</b>
 * <ul>
 *     <li>excludes（排除列表）：在列表中的异常不重试，直接抛出</li>
 *     <li>includes（包含列表）：仅列表中的异常重试</li>
 *     <li>两者都空：所有异常都重试</li>
 * </ul>
 *
 * @author xiyao
 * @see Retryable
 * @see RetryContext
 * @see GovernanceProperties.RetryConfig
 */
@Aspect
@Component
public class RetryAspect {
    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    /**
     * 治理配置属性
     */
    private final GovernanceProperties properties;

    /**
     * 构造函数
     *
     * @param properties 治理配置属性（非空）
     */
    public RetryAspect(GovernanceProperties properties) {
        this.properties = properties;
    }

    /**
     * 拦截 @Retryable 注解的方法，执行重试逻辑
     * <p>
     * 核心流程：
     * <ol>
     *     <li>获取重试配置（注解值优先，其次全局配置）</li>
     *     <li>获取异常过滤条件（includes/excludes）</li>
     *     <li>创建重试上下文</li>
     *     <li>循环执行直到成功或重试次数用尽</li>
     * </ol>
     *
     * @param point     切入点
     * @param retryable 重试注解配置
     * @return 目标方法的返回结果
     * @throws Throwable 如果重试次数用尽或异常不应该重试
     */
    @Around("@annotation(retryable)")
    public Object around(ProceedingJoinPoint point, Retryable retryable) throws Throwable {
        // 获取配置，优先使用注解值，否则使用全局配置
        int maxAttempts = retryable.maxAttempts() <= 0
                ? properties.getRetry().getMaxAttempts()
                : retryable.maxAttempts();
        long intervalMillis = retryable.intervalMillis() <= 0
                ? properties.getRetry().getIntervalMillis()
                : retryable.intervalMillis();
        double multiplier = retryable.multiplier() <= 0
                ? properties.getRetry().getMultiplier()
                : retryable.multiplier();

        // 获取异常类型过滤条件
        Class<? extends Throwable>[] includes = retryable.includes();
        Class<? extends Throwable>[] excludes = retryable.excludes();

        // 创建重试上下文，用于跟踪重试次数
        RetryContext context = new RetryContext(maxAttempts);

        // 重试循环
        while (true) {
            try {
                // 执行目标方法
                Object result = point.proceed();

                // 如果之前有重试（attemptCount > 1），记录成功日志
                if (context.getAttemptCount() > 1) {
                    log.info("重试成功: method={}, attempts={}",
                            getMethodKey(point), context.getAttemptCount());
                }
                return result;
            } catch (Throwable t) {
                // 检查是否应该重试
                if (!shouldRetry(t, includes, excludes)) {
                    // 异常不应该重试，直接抛出
                    throw t;
                }

                // 检查重试次数是否用尽
                int currentAttempt = context.incrementAndGet();
                if (context.isExhausted()) {
                    log.warn("重试次数用尽: method={}, attempts={}, error={}",
                            getMethodKey(point), currentAttempt, t.getMessage());
                    throw t;
                }

                // 计算等待时间
                long sleepTime = calculateSleepTime(currentAttempt, intervalMillis, multiplier);

                log.warn("重试中: method={}, attempt={}/{}, sleep={}ms, error={}",
                        getMethodKey(point), currentAttempt, maxAttempts, sleepTime, t.getMessage());

                // 等待后继续重试
                Thread.sleep(sleepTime);
            }
        }
    }

    /**
     * 判断异常是否应该重试
     * <p>
     * 异常过滤逻辑：
     * <ol>
     *     <li>excludes（排除列表）优先级最高，在列表中则不重试</li>
     *     <li>includes（包含列表）其次，不在列表中则不重试</li>
     *     <li>两者都为空，所有异常都重试</li>
     * </ol>
     *
     * @param t        抛出的异常
     * @param includes 可重试的异常类型数组
     * @param excludes 不可重试的异常类型数组
     * @return true 表示应该重试
     */
    private boolean shouldRetry(Throwable t, Class<? extends Throwable>[] includes,
                                Class<? extends Throwable>[] excludes) {
        // 如果有排除列表，检查是否在排除列表中
        if (excludes != null && excludes.length > 0) {
            for (Class<? extends Throwable> exclude : excludes) {
                // 使用 isAssignableFrom 检查是否是排除的异常类型（包括子类）
                if (exclude.isAssignableFrom(t.getClass())) {
                    return false;
                }
            }
        }

        // 如果有包含列表，检查是否在包含列表中
        if (includes != null && includes.length > 0) {
            for (Class<? extends Throwable> include : includes) {
                if (include.isAssignableFrom(t.getClass())) {
                    return true;
                }
            }
            // 不在包含列表中，不重试
            return false;
        }

        // 默认情况下，所有异常都重试
        return true;
    }

    /**
     * 计算重试等待时间
     * <p>
     * 根据 multiplier 判断使用固定间隔还是指数退避：
     * <ul>
     *     <li>multiplier <= 1.0：固定间隔，返回 intervalMillis</li>
     *     <li>multiplier > 1.0：指数退避，返回 intervalMillis * multiplier^(attempt-1)</li>
     * </ul>
     *
     * @param attempt        当前尝试次数（第几次重试）
     * @param intervalMillis 基础间隔时间
     * @param multiplier     指数退避倍数
     * @return 应该等待的时间（毫秒）
     */
    private long calculateSleepTime(int attempt, long intervalMillis, double multiplier) {
        if (multiplier > 1.0) {
            // 指数退避: interval * multiplier^(attempt-1)
            return (long) (intervalMillis * Math.pow(multiplier, attempt - 1));
        }
        // 固定间隔
        return intervalMillis;
    }

    /**
     * 获取方法唯一标识
     *
     * @param point 切入点
     * @return 方法唯一标识字符串
     */
    private String getMethodKey(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}