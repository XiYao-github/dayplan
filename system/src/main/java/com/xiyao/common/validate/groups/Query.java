package com.xiyao.common.validate.groups;

/**
 * 查询操作校验分组
 * <p>
 * 用于 @Validated 注解的分组校验，标识需要执行查询条件校验的方法参数。
 * 查询参数通常只需要基本的格式校验，不需要严格的存在性校验。
 *
 * <p>
 * <b>使用场景：</b>
 * <pre>{@code
 * // Controller 方法中使用
 * @GetMapping("/list")
 * public Result<PageResult<UserVO>> list(
 *         @ModelAttribute @Validated(Query.class) UserQueryDTO query) {
 *     // 查询用户列表时触发 Query 分组的校验
 * }
 *
 * // DTO 类中定义校验规则
 * public class UserQueryDTO extends PageQuery {
 *     // 查询条件宽松校验，允许部分条件为空
 *     @Size(min = 0, max = 20, message = "用户名长度需在0-20之间", groups = Query.class)
 *     private String username;
 *
 *     @Min(value = 0, message = "状态值不能小于0", groups = Query.class)
 *     @Max(value = 1, message = "状态值不能大于1", groups = Query.class)
 *     private Integer status;
 * }
 * }</pre>
 *
 * <p>
 * <b>分组优先级说明：</b>
 * <ul>
 *     <li>Add：新增时严格校验，所有必填字段不能为空</li>
 *     <li>Edit：编辑时校验，部分字段允许为空</li>
 *     <li>Query：查询时宽松校验，主要限制输入格式和范围</li>
 * </ul>
 *
 * @author xiyao
 * @see Add
 * @see Edit
 */
public interface Query {
}
