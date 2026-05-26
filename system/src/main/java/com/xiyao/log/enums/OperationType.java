package com.xiyao.log.enums;

import com.xiyao.log.annotation.AuditLog;

/**
 * 操作类型枚举
 * <p>
 * 用于分类操作日志的类型，便于日志统计和分析。
 * 使用 ordinal() 获取类型值，请确保顺序稳定，新增类型请添加到末尾。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @Log(module = "用户管理", operationType = OperationType.INSERT)
 * public Result&lt;User&gt; addUser(@RequestBody User user) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author xiyao
 * @see AuditLog
 */
public enum OperationType {

    /**
     * 其它操作
     * <p>
     * 用于不属于以下类型的操作，或未明确分类的操作。
     */
    OTHER,

    /**
     * 查询操作
     * <p>
     * 用于查询详情、列表等读操作。
     * 注意：查询操作通常不会修改数据，建议同时设置 isSaveRequestData = false。
     */
    QUERY,

    /**
     * 新增操作
     * <p>
     * 用于创建新资源的操作，如添加用户、创建订单等。
     */
    INSERT,

    /**
     * 更新操作
     * <p>
     * 用于修改已有资源的操作，如更新用户信息、修改订单状态等。
     */
    UPDATE,

    /**
     * 删除操作
     * <p>
     * 用于删除资源的操作，如删除用户、取消订单等。
     * 注意：删除操作应谨慎记录，建议设置 isSaveResponseData = false 避免敏感数据外泄。
     */
    DELETE,

    /**
     * 导出操作
     * <p>
     * 用于数据导出操作，如导出用户列表、导出报表等。
     */
    EXPORT,

    /**
     * 导入操作
     * <p>
     * 用于数据导入操作，如批量导入用户、导入订单数据等。
     */
    IMPORT,
}