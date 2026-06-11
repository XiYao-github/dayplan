package com.xiyao.common.validate.groups;

/**
 * 校验组：更新操作
 * <p>
 * 用于在 Entity 的校验注解中标识该校验规则适用于更新场景。
 * 通常与 @Validated 注解配合使用，通过 groups 属性指定校验组。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     @NotBlank(message = "用户名不能为空", groups = {Create.class, Update.class})
 *     private String username;
 *
 *     @Null(message = "更新时不能设置ID", groups = Update.class)
 *     private Long id;
 * }
 *
 * // Controller 中使用
 * @PutMapping("/{id}")
 * public Result<Void> update(@PathVariable Long id, @RequestBody @Validated({Update.class}) User user) {
 *     // ...
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Create
 * @see jakarta.validation.Valid
 */
public interface Update {
}