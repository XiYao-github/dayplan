package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 舱壁隔离注解
 * <p>
 * 基于线程池的舱壁模式（Bulkhead Pattern）实现并发隔离。
 * 用于限制同时执行某个方法的线程数量，防止资源耗尽。
 *
 * <p>
 * <b>设计原理：</b>
 * 舱壁模式源自船舶设计，通过隔板将船体分成多个独立密封舱。
 * 即使某个舱进水，也不会导致整艘船沉没。
 * 软件系统中，当某个服务出现故障时，舱壁隔离可以防止故障扩散。
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>方法被调用时，提交到专用线程池执行</li>
 *     <li>线程池维护固定数量的工作线程</li>
 *     <li>当线程池满时，新请求将被拒绝或排队等待</li>
 *     <li>其他方法的调用不受影响，实现资源隔离</li>
 * </ol>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>限制对下游服务的并发调用，防止对方过载</li>
 *     <li>隔离不同业务的执行，防止相互影响</li>
 *     <li>保护稀缺资源（如数据库连接池）不被耗尽</li>
 * </ul>
 *
 * <p>
 * <b>配置优先级：</b>
 * 注解属性值 &gt; 配置文件全局值（governance.bulkhead.*）
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Bulkhead(coreSize = 5, maxSize = 10, queueCapacity = 20)
 * @GetMapping("/api/user/{id}")
 * public Result<User> getUser(@PathVariable Long id) {
 *     return userService.getById(id);
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.bulkhead.BulkheadAspect
 * @see com.xiyao.governance.config.GovernanceProperties.BulkheadConfig
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bulkhead {

    /**
     * 线程池核心线程数
     * <p>
     * 线程池保持的最小线程数，即使处于空闲状态也不会回收。
     * 默认 -1 表示使用配置文件中的全局配置（governance.bulkhead.core-size）。
     *
     * @return 核心线程数
     */
    int coreSize() default -1;

    /**
     * 线程池最大线程数
     * <p>
     * 线程池允许的最大工作线程数量。
     * 当任务队列满时，线程池会扩容直到达到此值。
     * 默认 -1 表示使用配置文件中的全局配置（governance.bulkhead.max-size）。
     *
     * @return 最大线程数
     */
    int maxSize() default -1;

    /**
     * 等待队列容量
     * <p>
     * 任务队列的最大容量，当线程池都在忙碌时，新任务会在队列中等待。
     * 当队列满时，新请求将被拒绝（触发隔离）。
     * 默认 -1 表示使用配置文件中的全局配置（governance.bulkhead.queue-capacity）。
     *
     * @return 队列容量
     */
    int queueCapacity() default -1;

    /**
     * 隔离器名称
     * <p>
     * 用于区分不同的隔离池，实现资源分组管理。
     * 如果为空字符串（默认），则使用方法签名作为唯一标识。
     * <p>
     * 场景：多个方法共享同一个隔离池时，可以指定相同的名称。
     *
     * @return 隔离器名称
     */
    String name() default "";
}