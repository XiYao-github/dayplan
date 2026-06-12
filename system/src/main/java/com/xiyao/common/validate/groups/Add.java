package com.xiyao.common.validate.groups;

/**
 * 新增操作校验分组
 * <p>
 * 用于 @Validated 注解的分组校验，标识需要执行新增校验的方法参数。
 * 通常与实体类的 @NotNull、@NotBlank 等校验注解配合使用。
 *
 * <p>
 * <b>使用场景：</b>
 * <pre>{@code
 * // Controller 方法中使用
 * @PostMapping
 * public Result<Void> add(@RequestBody @Validated(Add.class) UserDTO dto) {
 *     // 新增用户时触发 Add 分组的校验
 * }
 *
 * // DTO 类中定义校验规则
 * public class UserDTO {
 *     @NotBlank(message = "用户名不能为空", groups = Add.class)
 *     private String username;
 *
 *     @NotNull(message = "密码不能为空", groups = Add.class)
 *     private String password;
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Query
 * @see Edit
 */
public interface Add {
}