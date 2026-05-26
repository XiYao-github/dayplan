package com.xiyao.governance.annotation;

import java.lang.annotation.*;

/**
 * 降级注解
 * <p>
 * 当目标方法抛出指定异常时，调用降级方法作为替代方案。
 * 用于在服务不可用时提供备用返回，避免直接抛出异常影响用户体验。
 *
 * <p>
 * <b>工作流程：</b>
 * <ol>
 *     <li>拦截带有 @Fallback 注解的方法调用</li>
 *     <li>执行目标方法，如果抛出异常</li>
 *     <li>检查异常类型是否匹配降级条件</li>
 *     <li>调用指定的降级方法，返回降级结果</li>
 * </ol>
 *
 * <p>
 * <b>降级方法签名：</b>
 * 降级方法必须接收一个 Throwable 参数，用于获取原始异常信息。
 * 返回类型建议与目标方法一致，但不做强制要求。
 * <pre>{@code
 * public Result<User> fallback(Throwable e) {
 *     return Result.error("服务繁忙，请稍后再试");
 * }
 * }</pre>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>下游服务不可用时，返回默认数据或缓存数据</li>
 *     <li>数据库连接失败时，返回友好的错误提示</li>
 *     <li>第三方API超时降级，提供本地兜底方案</li>
 *     <li>系统繁忙时，返回简化版的服务能力</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Fallback(fallbackClass = UserServiceFallback.class, value = {RuntimeException.class})
 * @GetMapping("/api/user/{id}")
 * public Result<User> getUser(@PathVariable Long id) {
 *     return userService.getById(id);
 * }
 *
 * // 降级处理类
 * public class UserServiceFallback {
 *     public Result<User> fallback(Throwable e) {
 *         // 可以记录日志、返回缓存或默认值
 *         return Result.ok(new User(-1L, "默认用户"));
 *     }
 * }
 * }</pre>
 *
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>降级方法可以是静态方法或实例方法</li>
 *     <li>实例方法会自动从 Spring 容器获取 Bean，如获取不到则创建新实例</li>
 *     <li>如果降级方法执行也失败，会返回错误结果而非抛出异常</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.governance.core.fallback.FallbackAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fallback {

    /**
     * 降级处理类
     * <p>
     * 指定包含降级方法的类，该类必须有无参构造方法或被 Spring 管理。
     *
     * @return 降级处理类
     */
    Class<?> fallbackClass();

    /**
     * 降级方法名
     * <p>
     * 降级方法的名称，方法必须接收一个 Throwable 类型参数。
     * 默认值为 "fallback"。
     *
     * @return 降级方法名
     */
    String fallbackMethod() default "fallback";

    /**
     * 触发降级的异常类型
     * <p>
     * 当目标方法抛出这些类型的异常时，触发降级调用。
     * 如果为空数组，则所有异常都会触发降级。
     * 支持异常继承关系，子类异常也会匹配父类声明。
     *
     * @return 触发降级的异常类型数组
     */
    Class<? extends Throwable>[] value() default {};
}