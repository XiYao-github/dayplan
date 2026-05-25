package com.xiyao.trace.annotation;

import java.lang.annotation.*;

/**
 * 链路追踪注解
 * <p>
 * 标注在 Controller/Service 方法上，自动记录方法调用的链路信息。
 *
 * <p>
 * <b>自动记录的信息：</b>
 * <ul>
 *     <li>traceId：追踪链 ID</li>
 *     <li>spanId：当前操作 ID</li>
 *     <li>方法名、参数、返回值</li>
 *     <li>执行耗时</li>
 *     <li>执行状态（成功/失败）</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Trace(module = "用户管理", operation = "查询用户")
 * @GetMapping("/user/{id}")
 * public Result&lt;User&gt; getUser(@PathVariable Long id) {
 *     return success(userService.getById(id));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.trace.aspect.TraceAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Trace {

    /**
     * 操作模块
     * <p>
     * 用于分类追踪记录，如"用户管理"、"订单管理"等。
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     * <p>
     * 具体操作的说明，如"查询用户"、"创建订单"等。
     *
     * @return 操作描述
     */
    String operation() default "";

    /**
     * 是否记录方法参数
     * <p>
     * 设置为 false 可避免记录敏感参数。
     *
     * @return true 记录参数，false 不记录
     */
    boolean isLogParams() default true;

    /**
     * 是否记录返回值
     * <p>
     * 返回值较大时建议设置为 false。
     *
     * @return true 记录返回值，false 不记录
     */
    boolean isLogResult() default false;
}