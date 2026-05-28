package com.xiyao.log.enums;

/**
 * 日志类型枚举
 * <p>
 * 用于区分操作日志和审计日志，决定日志记录的存储策略和安全等级。
 * 通过 @Log 注解的 logType 属性指定。
 *
 * <p>
 * <b>类型说明：</b>
 * <ul>
 *     <li>OPERATION：操作日志，用于日常业务追踪，无需哈希链防篡改</li>
 *     <li>AUDIT：审计日志，用于等保合规审计，必须有 SM3 哈希链防篡改</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 记录操作日志（默认）
 * @Log(module = "用户管理", type = OperationType.INSERT)
 * public Result createUser(User user) { ... }
 *
 * // 记录审计日志（敏感操作）
 * @Log(module = "权限管理", type = OperationType.DELETE, logType = LogType.AUDIT)
 * public Result deleteRole(Long id) { ... }
 * }</pre>
 *
 * @author xiyao
 * @see com.xiyao.log.annotation.Log
 */
public enum LogType {

    /**
     * 操作日志（业务追踪）
     * <p>
     * 用于记录日常业务操作，便于问题排查和业务追踪。
     * 无需哈希链防篡改，普通管理员可查询。
     */
    OPERATION,

    /**
     * 审计日志（合规审计）
     * <p>
     * 用于记录敏感操作，满足等保合规要求。
     * 必须有 SM3 哈希链防篡改，仅审计管理员可查询。
     */
    AUDIT
}