package com.xiyao.governance.core.bulkhead;

import com.xiyao.framework.exception.BusinessException;
import com.xiyao.governance.annotation.Bulkhead;
import com.xiyao.governance.config.GovernanceProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * 舱壁隔离切面
 * <p>
 * 基于线程池的舱壁模式（Bulkhead Pattern）实现并发隔离。
 * 使用 AOP 拦截带有 @Bulkhead 注解的方法，将方法调用提交到专用线程池执行。
 *
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>方法被调用时，检查线程池的活跃线程数</li>
 *     <li>如果活跃线程数已达最大值，表示资源紧张，抛出异常拒绝调用</li>
 *     <li>否则，将方法调用提交到线程池异步执行</li>
 *     <li>主线程阻塞等待执行结果（使用 Future.get）</li>
 * </ol>
 *
 * <p>
 * <b>与信号量隔离的区别：</b>
 * <ul>
 *     <li>线程池隔离：使用独立线程执行，可以控制线程数量和队列大小</li>
 *     <li>信号量隔离：在当前线程执行，仅限制并发数量</li>
 * </ul>
 *
 * <p>
 * <b>线程池管理策略：</b>
 * <ul>
 *     <li>每个唯一的隔离名称对应一个独立的线程池</li>
 *     <li>线程池懒创建，首次使用时创建</li>
 *     <li>使用 CallerRunsPolicy 拒绝策略，将任务回退给调用者线程</li>
 * </ul>
 *
 * @author xiyao
 * @see Bulkhead
 * @see BulkheadManager
 * @see GovernanceProperties.BulkheadConfig
 */
@Aspect
@Component
public class BulkheadAspect {
    private static final Logger log = LoggerFactory.getLogger(BulkheadAspect.class);

    /**
     * 治理配置属性
     */
    private final GovernanceProperties properties;

    /**
     * 舱壁管理器，负责管理多个隔离线程池
     */
    private final BulkheadManager bulkheadManager;

    /**
     * 构造函数
     * <p>
     * 初始化时创建 BulkheadManager，使用配置文件中的全局默认值。
     *
     * @param properties 治理配置属性（非空）
     */
    public BulkheadAspect(GovernanceProperties properties) {
        this.properties = properties;
        this.bulkheadManager = new BulkheadManager(
                properties.getBulkhead().getCoreSize(),
                properties.getBulkhead().getMaxSize(),
                properties.getBulkhead().getQueueCapacity()
        );
    }

    /**
     * 拦截 @Bulkhead 注解的方法，执行舱壁隔离逻辑
     * <p>
     * 核心流程：
     * <ol>
     *     <li>获取或创建该隔离名称对应的线程池</li>
     *     <li>检查活跃线程数是否已满</li>
     *     <li>将方法调用提交到线程池执行</li>
     *     <li>获取执行结果返回</li>
     * </ol>
     *
     * @param point   切入点，包含目标方法信息
     * @param bulkhead 舱壁注解配置
     * @return 目标方法的返回结果
     * @throws Throwable 如果执行被拒绝、方法执行失败或出现异常
     */
    @Around("@annotation(bulkhead)")
    public Object around(ProceedingJoinPoint point, Bulkhead bulkhead) throws Throwable {
        // 获取或创建隔离线程池
        String name = getBulkheadName(point, bulkhead);

        // 优先使用注解配置，否则使用全局配置
        int coreSize = bulkhead.coreSize() <= 0
                ? properties.getBulkhead().getCoreSize()
                : bulkhead.coreSize();
        int maxSize = bulkhead.maxSize() <= 0
                ? properties.getBulkhead().getMaxSize()
                : bulkhead.maxSize();
        int queueCapacity = bulkhead.queueCapacity() <= 0
                ? properties.getBulkhead().getQueueCapacity()
                : bulkhead.queueCapacity();

        // 获取或创建线程池
        ThreadPoolExecutor executor = this.bulkheadManager.getOrCreate(name, coreSize, maxSize, queueCapacity);

        // 检查线程池是否已满（活跃线程数 >= 最大线程数）
        if (executor.getActiveCount() >= maxSize) {
            log.warn("隔离触发（线程池已满）: name={}, active={}, max={}",
                    name, executor.getActiveCount(), maxSize);
            throw new BusinessException("服务繁忙，请稍后再试");
        }

        try {
            // 获取方法签名和参数
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Object[] args = point.getArgs();

            // 获取目标对象（用于反射调用）
            Object target = point.getTarget();

            // 提交到线程池执行
            // 使用 Lambda 表达式捕获方法调用，避免直接序列化问题
            Future<Object> future = executor.submit(() -> {
                try {
                    // 通过反射调用目标方法
                    // 注意：此处调用的是实例方法，需要传入 target 对象
                    return method.invoke(target, args);
                } catch (Exception e) {
                    // 反射调用产生的异常包装为 RuntimeException
                    throw new RuntimeException(e);
                }
            });

            // 阻塞等待执行结果
            return future.get();
        } catch (ExecutionException e) {
            // 提取原始异常
            Throwable cause = e.getCause();
            // 如果是 RuntimeException，取其-cause（原始异常）
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                throw cause.getCause();
            }
            throw cause != null ? cause : e;
        } catch (InterruptedException e) {
            // 线程被中断，恢复中断状态并抛出业务异常
            Thread.currentThread().interrupt();
            throw new BusinessException("请求被中断");
        }
    }

    /**
     * 获取隔离名称
     * <p>
     * 如果注解指定了名称，使用指定名称；
     * 否则使用方法签名（类名.方法名）作为唯一标识。
     *
     * @param point    切入点
     * @param bulkhead 舱壁注解
     * @return 隔离名称
     */
    private String getBulkheadName(ProceedingJoinPoint point, Bulkhead bulkhead) {
        String name = bulkhead.name();
        if (name == null || name.isEmpty()) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            // 使用类的全限定名 + 方法名作为标识
            name = method.getDeclaringClass().getName() + "." + method.getName();
        }
        return name;
    }
}