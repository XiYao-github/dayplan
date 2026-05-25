package com.xiyao.governance.core.ratelimit;

import com.xiyao.common.utils.SpringUtils;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.governance.annotation.RateLimit;
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
 * 限流切面
 * <p>
 * 使用令牌桶算法实现基于每秒令牌数的限流控制。
 * 通过 AOP 拦截带有 @RateLimit 注解的方法，自动进行流量控制。
 *
 * <p>
 * <b>令牌桶算法说明：</b>
 * <ul>
 *     <li>桶以固定速率（permitsPerSecond）补充令牌</li>
 *     <li>桶有最大容量（maxBurstRequests），超出容量的令牌被丢弃</li>
 *     <li>请求时从桶中获取令牌，获取到才放行</li>
 *     <li>桶空时请求被拒绝，实现限流</li>
 * </ul>
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>获取或创建该方法的限流器</li>
 *     <li>尝试从令牌桶获取令牌</li>
 *     <li>获取成功则执行方法，获取失败则抛出异常</li>
 * </ol>
 *
 * <p>
 * <b>突发流量处理：</b>
 * 令牌桶允许一定程度的突发流量。
 * 例如：每秒补充 100 个令牌，最大容量 50。
 * 瞬时可以消耗最多 150 个令牌（100 + 50）。
 *
 * @author xiyao
 * @see RateLimit
 * @see RateLimiter
 * @see GovernanceProperties.RateLimitConfig
 */
@Aspect
@Component
public class RateLimitAspect {
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    /**
     * 治理配置属性
     */
    private final GovernanceProperties properties;

    /**
     * 限流器管理器
     * <p>
     * 负责管理多个方法的限流器，使用方法签名作为 key。
     * 内部使用 ConcurrentHashMap 保证线程安全。
     */
    private final RateLimiter rateLimiter;

    /**
     * 构造函数
     *
     * @param properties 治理配置属性（非空）
     */
    public RateLimitAspect(GovernanceProperties properties) {
        this.properties = properties;
        // 使用配置文件中的全局值初始化限流器
        this.rateLimiter = new RateLimiter(
                properties.getRateLimit().getPermitsPerSecond(),
                properties.getRateLimit().getMaxBurstRequests()
        );
    }

    /**
     * 拦截 @RateLimit 注解的方法，执行限流逻辑
     * <p>
     * 核心流程：
     * <ol>
     *     <li>获取注解配置（优先使用注解值，其次使用全局配置）</li>
     *     <li>根据方法签名生成 key，获取或创建限流器</li>
     *     <li>尝试获取令牌</li>
     *     <li>获取成功执行方法，获取失败抛出 BusinessException</li>
     * </ol>
     *
     * @param point     切入点
     * @param rateLimit 限流注解配置
     * @return 目标方法的返回结果
     * @throws Throwable 如果限流触发或方法执行失败
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取注解配置，优先使用注解值，否则使用全局配置
        double permitsPerSecond = rateLimit.permitsPerSecond() <= 0
                ? properties.getRateLimit().getPermitsPerSecond()
                : rateLimit.permitsPerSecond();
        int maxBurstRequests = rateLimit.maxBurstRequests() <= 0
                ? properties.getRateLimit().getMaxBurstRequests()
                : rateLimit.maxBurstRequests();
        String message = rateLimit.message();
        // 如果未设置消息，使用全局配置的消息
        if (message == null || message.isEmpty()) {
            message = properties.getRateLimit().getMessage();
        }

        // 获取或创建该方法的限流器
        String key = getKey(point);
        RateLimiter limiter = rateLimiter.getOrCreate(key, permitsPerSecond, maxBurstRequests);

        // 尝试获取令牌
        if (!limiter.tryAcquire()) {
            log.warn("限流触发: key={}, message={}", key, message);
            throw new BusinessException(message);
        }

        // 执行目标方法
        return point.proceed();
    }

    /**
     * 生成限流 key
     * <p>
     * 使用类的全限定名 + 方法名作为唯一标识。
     * 不同方法拥有不同的限流器，独立进行流量控制。
     *
     * @param point 切入点
     * @return 限流 key
     */
    private String getKey(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}