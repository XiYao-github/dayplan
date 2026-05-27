package com.xiyao.governance.core.fallback;

import com.xiyao.common.utils.data.Result;
import com.xiyao.governance.annotation.Fallback;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 降级切面
 * <p>
 * 当目标方法抛出指定异常时，调用降级方法作为替代方案。
 * 使用 AOP 拦截带有 @Fallback 注解的方法，实现优雅降级。
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>执行目标方法</li>
 *     <li>如果正常返回，直接返回结果</li>
 *     <li>如果抛出异常，检查异常类型是否匹配</li>
 *     <li>如果匹配，调用降级方法返回</li>
 *     <li>如果不匹配，继续抛出原异常</li>
 * </ol>
 *
 * <p>
 * <b>异常匹配逻辑：</b>
 * <ul>
 *     <li>如果 value 为空数组，所有异常都触发降级</li>
 *     <li>如果 value 有值，仅匹配列表中的异常类型</li>
 *     <li>支持异常继承关系，子类异常会匹配父类声明</li>
 * </ul>
 *
 * <p>
 * <b>降级方法获取：</b>
 * <ul>
 *     <li>静态方法：直接通过类名调用</li>
 *     <li>实例方法：尝试从 Spring 容器获取 Bean，如获取不到则创建新实例</li>
 * </ul>
 *
 * @author xiyao
 * @see Fallback
 * @see com.xiyao.governance.annotation.Fallback
 */
@Aspect
public class FallbackAspect {
    private static final Logger log = LoggerFactory.getLogger(FallbackAspect.class);

    /**
     * 拦截 @Fallback 注解的方法，执行降级逻辑
     * <p>
     * 核心流程：
     * <ol>
     *     <li>执行目标方法</li>
     *     <li>捕获异常</li>
     *     <li>检查异常类型是否匹配降级条件</li>
     *     <li>调用降级方法返回结果</li>
     * </ol>
     *
     * @param point    切入点
     * @param fallback 降级注解配置
     * @return 目标方法的返回结果或降级结果
     * @throws Throwable 如果降级方法调用失败或异常不匹配
     */
    @Around("@annotation(fallback)")
    public Object around(ProceedingJoinPoint point, Fallback fallback) throws Throwable {
        try {
            // 执行目标方法
            return point.proceed();
        } catch (Throwable t) {
            // 检查异常类型是否匹配
            Class<? extends Throwable>[] includeTypes = fallback.value();

            // 如果有指定异常类型，进行匹配检查
            if (includeTypes != null && includeTypes.length > 0) {
                boolean matched = false;
                for (Class<? extends Throwable> includeType : includeTypes) {
                    // 使用 isAssignableFrom 检查异常匹配（包括子类）
                    if (includeType.isAssignableFrom(t.getClass())) {
                        matched = true;
                        break;
                    }
                }
                // 如果异常类型不匹配，继续抛出原异常
                if (!matched) {
                    throw t;
                }
            }

            // 调用降级方法
            return invokeFallback(point, fallback, t);
        }
    }

    /**
     * 调用降级方法
     * <p>
     * 获取降级处理类的实例，然后通过反射调用降级方法。
     * 降级方法必须接收一个 Throwable 参数。
     *
     * <p>
     * <b>实例获取逻辑：</b>
     * <ol>
     *     <li>如果是静态方法，直接调用</li>
     *     <li>如果是实例方法，尝试从 Spring 容器获取</li>
     *     <li>如果容器获取失败，创建新实例</li>
     * </ol>
     *
     * @param point             切入点
     * @param fallback          降级注解配置
     * @param originalException 原始异常
     * @return 降级方法的返回结果
     */
    private Object invokeFallback(ProceedingJoinPoint point, Fallback fallback, Throwable originalException) {
        Class<?> fallbackClass = fallback.fallbackClass();
        String fallbackMethodName = fallback.fallbackMethod();

        try {
            // 获取降级方法，参数为 Throwable 类型
            Method fallbackMethod = fallbackClass.getMethod(fallbackMethodName, Throwable.class);
            Object fallbackInstance = null;

            // 如果降级方法不是静态的，需要获取降级类的实例
            if (!java.lang.reflect.Modifier.isStatic(fallbackMethod.getModifiers())) {
                // 尝试通过 Spring 容器获取降级类实例
                try {
                    fallbackInstance = com.xiyao.common.utils.SpringUtils.getBean(fallbackClass);
                } catch (Exception e) {
                    // 如果获取不到，创建新实例（降级方法可能不需要 Spring 依赖）
                    fallbackInstance = fallbackClass.getDeclaredConstructor().newInstance();
                }
            }

            // 记录降级触发日志
            log.info("触发降级: fallbackClass={}, method={}, originalException={}",
                    fallbackClass.getName(), fallbackMethodName, originalException.getClass().getName());

            // 调用降级方法
            if (fallbackInstance != null) {
                // 实例方法调用
                return fallbackMethod.invoke(fallbackInstance, originalException);
            } else {
                // 静态方法调用
                return fallbackMethod.invoke(null, originalException);
            }
        } catch (Exception e) {
            // 降级方法调用失败，记录错误日志
            log.error("降级方法调用失败: fallbackClass={}, method={}", fallbackClass.getName(), fallbackMethodName, e);
            // 降级失败，返回错误结果而非抛出异常，避免异常链继续传播
            return Result.error(originalException.getMessage());
        }
    }
}