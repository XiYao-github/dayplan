package com.xiyao.log.enums;

/**
 * 操作类型枚举
 * <p>
 * 用于分类日志的类型，便于统计和分析。
 * 使用 ordinal() 获取类型值，请确保顺序稳定，新增类型请添加到末尾。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Log(module = "用户管理", type = OperationType.INSERT)
 * public Result&lt;User&gt; addUser(@RequestBody User user) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author xiyao
 */
public enum OperationType {

    /**
     * 其它操作
     */
    OTHER,

    /**
     * 查询操作
     */
    QUERY,

    /**
     * 新增操作
     */
    INSERT,

    /**
     * 更新操作
     */
    UPDATE,

    /**
     * 删除操作
     */
    DELETE,

    /**
     * 导出操作
     */
    EXPORT,

    /**
     * 导入操作
     */
    IMPORT,

    /**
     * 登录操作
     */
    LOGIN,

    /**
     * 登出操作
     */
    LOGOUT,

    /**
     * 注册操作
     */
    REGISTER
}