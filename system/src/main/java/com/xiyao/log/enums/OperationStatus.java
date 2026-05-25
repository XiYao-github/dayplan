package com.xiyao.log.enums;

/**
 * 操作状态枚举
 * <p>
 * 用于标识操作日志的执果状态：成功或失败。
 * <p>
 * 该枚举与数据库表字段对应，使用 ordinal() 获取状态值：
 * <ul>
 *     <li>SUCCESS.ordinal() = 0：成功</li>
 *     <li>FAIL.ordinal() = 1：失败</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.log.aspect.LogAspect
 */
public enum OperationStatus {

    /**
     * 成功
     * <p>
     * 表示操作正常执行并完成，业务逻辑处理成功。
     */
    SUCCESS,

    /**
     * 失败
     * <p>
     * 表示操作执行过程中发生异常，业务逻辑处理失败。
     * 通常配合异常信息共同记录。
     */
    FAIL,
}