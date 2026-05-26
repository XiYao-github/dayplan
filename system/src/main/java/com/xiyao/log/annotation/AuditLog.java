package com.xiyao.log.annotation;


import com.xiyao.log.enums.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志。
 * 配合 LogAspect 切面实现，在方法执行前后自动记录日志信息。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @PostMapping("/user")
 * @Log(module = "用户管理", operationType = OperationType.INSERT, isSaveRequestData = true, isSaveResponseData = false)
 * public Result&lt;User&gt; addUser(@RequestBody User user) {
 *     return Result.ok(userService.add(user));
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.log.aspect.LogAspect
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作模块
     * <p>
     * 用于分类日志，如"用户管理"、"角色管理"、"订单管理"等。
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     * <p>
     * 指定操作的类型，用于日志分类和统计。
     *
     * @return 操作类型枚举值
     * @see OperationType
     */
    OperationType operationType() default OperationType.OTHER;

    /**
     * 是否保存请求参数
     * <p>
     * 设置为 true 时，会将方法参数序列化为 JSON 并保存到日志。
     * 对于包含敏感信息的参数建议设置为 false。
     *
     * @return true 保存请求参数，false 不保存
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     * <p>
     * 设置为 true 时，会将方法返回值序列化为 JSON 并保存到日志。
     * 返回数据较大时建议设置为 false 以节省存储空间。
     *
     * @return true 保存响应数据，false 不保存
     */
    boolean isSaveResponseData() default true;
}
