package com.xiyao.governance.core.circuit;

import com.xiyao.framework.exception.BusinessException;
import com.xiyao.governance.annotation.CircuitBreaker;
import com.xiyao.governance.config.GovernanceProperties;
import com.xiyao.governance.enums.CircuitState;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 熔断切面
 * <p>
 * 基于失败率的三状态熔断器（Circuit Breaker）实现。
 * 使用 AOP 拦截带有 @CircuitBreaker 注解的方法，自动统计调用结果并控制熔断状态。
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>检查熔断器是否允许请求（根据当前状态）</li>
 *     <li>如果熔断开启（OPEN），直接抛出异常拒绝调用</li>
 *     <li>如果状态为半开（HALF_OPEN），允许试探性请求</li>
 *     <li>正常执行方法调用</li>
 *     <li>根据调用结果记录成功/失败</li>
 *     <li>更新熔断器状态（可能触发状态转换）</li>
 * </ol>
 *
 * <p>
 * <b>熔断触发条件：</b>
 * <ul>
 *     <li>失败次数达到 failureRateThreshold</li>
 *     <li>错误率达到 errorRateThreshold（需超过 minRequestNumber）</li>
 * </ul>
 *
 * <p>
 * <b>恢复条件：</b>
 * <ul>
 *     <li>熔断持续时间超过 breakDurationMillis</li>
 *     <li>半开状态下成功次数达到 successRateThreshold</li>
 * </ul>
 *
 * @author xiyao
 * @see CircuitBreaker
 * @see CircuitBreakerManager
 * @see CircuitBreakerState
 * @see CircuitState
 * @see GovernanceProperties.CircuitBreakerConfig
 */
@Aspect
@Component
public class CircuitBreakerAspect {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerAspect.class);

    /**
     * 治理配置属性
     */
    private final GovernanceProperties properties;

    /**
     * 熔断器管理器，负责管理多个方法的熔断状态
     */
    private final CircuitBreakerManager manager;

    /**
     * 构造函数
     *
     * @param properties 治理配置属性（非空）
     */
    public CircuitBreakerAspect(GovernanceProperties properties) {
        this.properties = properties;
        this.manager = new CircuitBreakerManager();
    }

    /**
     * 拦截 @CircuitBreaker 注解的方法，执行熔断控制逻辑
     * <p>
     * 核心流程：
     * <ol>
     *     <li>构建方法唯一标识 key</li>
     *     <li>获取注解配置（优先注解值，其次全局配置）</li>
     * <li>获取或创建该方法的熔断器状态</li>
     *     <li>检查是否允许请求</li>
     *     <li>执行目标方法</li>
     *     <li>记录调用结果</li>
     *     <li>根据结果更新熔断器状态</li>
     * </ol>
     *
     * @param point         切入点，包含目标方法信息
     * @param circuitBreaker 熔断注解配置
     * @return 目标方法的返回结果
     * @throws Throwable 如果请求被拒绝、方法执行失败或出现异常
     */
    @Around("@annotation(circuitBreaker)")
    public Object around(ProceedingJoinPoint point, CircuitBreaker circuitBreaker) throws Throwable {
        // 生成方法唯一标识
        String key = getKey(point);

        // 获取配置，优先使用注解值，否则使用全局配置
        int failureRateThreshold = circuitBreaker.failureRateThreshold() <= 0
                ? properties.getCircuitBreaker().getFailureRateThreshold()
                : circuitBreaker.failureRateThreshold();
        int successRateThreshold = circuitBreaker.successRateThreshold() <= 0
                ? properties.getCircuitBreaker().getSuccessRateThreshold()
                : circuitBreaker.successRateThreshold();
        long windowSizeMillis = circuitBreaker.windowSizeMillis() <= 0
                ? properties.getCircuitBreaker().getWindowSizeMillis()
                : circuitBreaker.windowSizeMillis();
        long breakDurationMillis = circuitBreaker.breakDurationMillis() <= 0
                ? properties.getCircuitBreaker().getBreakDurationMillis()
                : circuitBreaker.breakDurationMillis();
        double errorRateThreshold = circuitBreaker.errorRateThreshold() <= 0
                ? properties.getCircuitBreaker().getErrorRateThreshold()
                : circuitBreaker.errorRateThreshold();
        int minRequestNumber = circuitBreaker.minRequestNumber() <= 0
                ? properties.getCircuitBreaker().getMinRequestNumber()
                : circuitBreaker.minRequestNumber();

        // 获取或创建熔断器状态
        CircuitBreakerState state = manager.getOrCreate(key, failureRateThreshold, successRateThreshold,
                windowSizeMillis, breakDurationMillis, errorRateThreshold, minRequestNumber);

        // 检查是否允许请求
        if (!manager.isCallPermitted(key)) {
            log.warn("熔断器触发（OPEN状态）: key={}", key);
            throw new BusinessException("服务暂不可用，请稍后再试");
        }

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 记录成功
            manager.recordSuccess(key);

            // 如果状态变为 CLOSED 且刚完成恢复（状态切换时间小于 100ms），记录恢复日志
            if (state.getState() == CircuitState.CLOSED &&
                    System.currentTimeMillis() - state.getStateChangedTime().get() < 100) {
                log.info("熔断器已恢复（CLOSED状态）: key={}", key);
            }

            return result;
        } catch (Throwable t) {
            // 记录失败
            manager.recordFailure(key);

            // 如果记录失败后状态变为 OPEN，记录警告日志
            if (state.getState() == CircuitState.OPEN) {
                log.warn("熔断器触发（记录失败后OPEN）: key={}, error={}", key, t.getMessage());
            }

            throw t;
        }
    }

    /**
     * 生成熔断器唯一标识
     * <p>
     * 使用类的全限定名 + 方法名作为标识，确保不同类的方法不会混淆。
     *
     * @param point 切入点
     * @return 方法唯一标识字符串
     */
    private String getKey(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}