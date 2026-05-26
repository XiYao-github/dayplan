package com.xiyao.log.annotation;

import com.xiyao.log.enums.LogType;
import com.xiyao.log.enums.OperationType;

import java.lang.annotation.*;

/**
 * 日志记录注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志或审计日志。
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 记录操作日志（默认，简化写法）
 * @Log(module = "用户管理", type = OperationType.INSERT)
 * public Result create(@RequestBody User user) { ... }
 *
 * // 记录审计日志（需要防篡改、三员隔离）
 * @Log(module = "权限管理", type = OperationType.DELETE, logType = LogType.AUDIT)
 * public Result deleteRole(Long id) { ... }
 * }</pre>
 *
 * @author xiyao
 * @see LogType
 * @see OperationType
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     * <p>
     * 例如：用户管理、订单管理、权限管理
     */
    String module();

    /**
     * 操作类型
     * <p>
     * 用于分类和检索日志记录。
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 日志类型
     * <p>
     * 默认 OPERATION（操作日志），敏感操作使用 AUDIT（审计日志）。
     * AUDIT 类型会自动添加哈希链防篡改，仅审计管理员可查询。
     *
     * @return 日志类型，默认 OPERATION
     */
    LogType logType() default LogType.OPERATION;

    /**
     * 是否保存请求参数
     * <p>
     * 设为 true 时，会将方法参数 JSON 序列化后存入日志。
     *
     * @return true 保存，false 不保存
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     * <p>
     * 设为 true 时，会将方法返回值 JSON 序列化后存入日志。
     *
     * @return true 保存，false 不保存
     */
    boolean isSaveResponseData() default false;
}