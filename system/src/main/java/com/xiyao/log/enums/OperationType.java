package com.xiyao.log.enums;

/**
 * 操作类型枚举
 * <p>
 * 用于分类日志的操作类型，便于统计、分析和检索。
 * 使用 ordinal() 获取类型值，新增类型请添加到末尾以保持兼容性。
 *
 * <p>
 * <b>类型值说明：</b>
 * <ul>
 *     <li>0 - OTHER：其它操作（默认）</li>
 *     <li>1 - QUERY：查询操作</li>
 *     <li>2 - INSERT：新增操作</li>
 *     <li>3 - UPDATE：更新操作</li>
 *     <li>4 - DELETE：删除操作</li>
 *     <li>5 - EXPORT：导出操作</li>
 *     <li>6 - IMPORT：导入操作</li>
 *     <li>7 - LOGIN：登录操作</li>
 *     <li>8 - LOGOUT：登出操作</li>
 *     <li>9 - REGISTER：注册操作</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 记录新增用户操作
 * @Log(module = "用户管理", type = OperationType.INSERT)
 * public Result createUser(@RequestBody User user) {
 *     // 业务逻辑
 * }
 *
 * // 记录删除角色操作（审计日志）
 * @Log(module = "权限管理", type = OperationType.DELETE, logType = LogType.AUDIT)
 * public Result deleteRole(Long id) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.log.annotation.Log
 */
public enum OperationType {

    /**
     * 其它操作
     * <p>
     * 当操作类型不属于以下类型时使用，作为默认值。
     */
    OTHER,

    /**
     * 查询操作
     * <p>
     * 读取数据操作，如列表查询、详情查询等。
     */
    QUERY,

    /**
     * 新增操作
     * <p>
     * 创建数据操作，如新增用户、新增角色等。
     */
    INSERT,

    /**
     * 更新操作
     * <p>
     * 修改数据操作，如更新用户信息、更新配置等。
     */
    UPDATE,

    /**
     * 删除操作
     * <p>
     * 删除数据操作，如删除用户、删除角色等。
     */
    DELETE,

    /**
     * 导出操作
     * <p>
     * 数据导出操作，如导出 Excel、导出 PDF 等。
     */
    EXPORT,

    /**
     * 导入操作
     * <p>
     * 数据导入操作，如导入 Excel、批量导入等。
     */
    IMPORT,

    /**
     * 登录操作
     * <p>
     * 用户登录认证操作。
     */
    LOGIN,

    /**
     * 登出操作
     * <p>
     * 用户退出登录操作。
     */
    LOGOUT,

    /**
     * 注册操作
     * <p>
     * 用户注册账号操作。
     */
    REGISTER
}