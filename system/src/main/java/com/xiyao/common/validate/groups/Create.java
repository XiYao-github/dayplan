package com.xiyao.common.validate.groups;

/**
 * 校验组：新增操作
 * <p>
 * 用于在 Entity 的校验注解中标识该校验规则适用于新增场景。
 * 通常与 @Validated 注解配合使用，通过 groups 属性指定校验组。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     @NotBlank(message = "用户名不能为空", groups = {Create.class, Update.class})
 *     private String username;
 *
 *     @NotNull(message = "密码不能为空", groups = Create.class)
 *     private String password;
 * }
 *
 * // Controller 中使用
 * @PostMapping
 * public Result<Void> create(@RequestBody @Validated({Create.class}) User user) {
 *     // ...
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Update
 * @see jakarta.validation.Valid
 */
public interface Create {
}