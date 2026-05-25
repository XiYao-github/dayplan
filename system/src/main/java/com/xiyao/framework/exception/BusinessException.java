package com.xiyao.framework.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义业务异常
 * <p>
 * 用于在业务逻辑中抛出明确的业务错误，
 * 如：参数校验失败、权限不足、数据不存在等。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 简单用法
 * if (user == null) {
 *     throw new BusinessException("用户不存在");
 * }
 *
 * // 指定错误码
 * throw new BusinessException(400, "参数错误");
 *
 * // 携带原始异常
 * try {
 *     doSomething();
 * } catch (IOException e) {
 *     throw new BusinessException("操作失败", e);
 * }
 * }</pre>
 *
 * <p>
 * 全局异常处理器 {@link GlobalExceptionHandler} 会统一捕获此异常，
 * 并根据 errorCode 返回对应的 HTTP 状态码和响应消息。
 *
 * @author xiyao
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 默认错误码（HTTP 500）
     */
    public static final int ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数（仅消息）
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ERROR;
        this.message = message;
    }

    /**
     * 构造函数（错误码 + 消息）
     *
     * @param code    HTTP 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数（消息 + 原始异常）
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ERROR;
        this.message = message;
    }

    /**
     * 构造函数（错误码 + 消息 + 原始异常）
     *
     * @param code    HTTP 错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}