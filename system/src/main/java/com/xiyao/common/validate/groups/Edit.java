package com.xiyao.common.validate.groups;

/**
 * 编辑操作校验分组
 * <p>
 * 用于 @Validated 注解的分组校验，标识需要执行编辑校验的方法参数。
 * 与 Add 分组的区别：编辑时某些字段允许为空（如密码可以为空，表示不修改密码）。
 *
 * <p>
 * <b>使用场景：</b>
 * <pre>{@code
 * // Controller 方法中使用
 * @PutMapping
 * public Result<Void> update(@RequestBody @Validated(Edit.class) UserDTO dto) {
 *     // 更新用户时触发 Edit 分组的校验
 * }
 *
 * // DTO 类中定义校验规则
 * public class UserDTO {
 *     @NotNull(message = "用户ID不能为空", groups = Edit.class)
 *     private Long id;
 *
 *     // 编辑时用户名仍需校验
 *     @NotBlank(message = "用户名不能为空", groups = {Add.class, Edit.class})
 *     private String username;
 *
 *     // 密码在编辑时可以为空（不修改密码）
 *     @Blank(message = "密码不能修改", groups = Edit.class)
 *     private String password;
 * }
 * }</pre>
 *
 * @author xiyao
 * @see Query
 * @see Add
 */
public interface Edit {
}