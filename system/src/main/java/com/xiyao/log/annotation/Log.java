package com.xiyao.log.annotation;

import com.xiyao.log.enums.OperationType;

import java.lang.annotation.*;

/**
 * 日志记录注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志。
 * 配合 AOP 切面 LogAspect 使用，在方法执行前后自动记录日志信息。
 *
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>自动获取当前登录用户信息（userId、username）</li>
 *     <li>自动获取请求信息（IP、设备信息、请求参数等）</li>
 *     <li>自动计算方法执行耗时</li>
 *     <li>支持请求参数和响应结果的 JSON 保存</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 记录操作日志
 * @Log(module = "用户管理", type = OperationType.INSERT)
 * public Result createUser(@RequestBody User user) {
 *     // 业务逻辑
 * }
 *
 * // 保存请求参数和响应结果
 * @Log(module = "订单管理", type = OperationType.INSERT,
 *      isSaveRequestData = true, isSaveResponseData = true)
 * public Result createOrder(@RequestBody Order order) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author xiyao
 * @see OperationType
 * @see com.xiyao.log.aspect.LogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 操作模块
     * <p>
     * 用于标识日志所属的业务模块，便于分类检索。
     * 例如：用户管理、订单管理、权限管理、角色管理等。
     *
     * @return 操作模块名称
     */
    String module();

    /**
     * 操作类型
     * <p>
     * 用于分类和检索日志记录，标识当前操作的动作类型。
     * 默认值为 OTHER。
     *
     * @return 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 是否保存请求参数
     * <p>
     * 设为 true 时，会将方法参数 JSON 序列化后存入日志。
     * 敏感参数（如密码）建议在业务层处理后再记录。
     *
     * @return true 保存，false 不保存
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     * <p>
     * 设为 true 时，会将方法返回值 JSON 序列化后存入日志。
     * 大数据量响应建议关闭，避免日志过大。
     *
     * @return true 保存，false 不保存
     */
    boolean isSaveResponseData() default false;
}