package com.xiyao.framework.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 失败码
     */
    public static final int ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 错误信息
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ERROR;
        this.message = message;
    }

    /**
     * 错误码,错误信息
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 错误信息,原始异常
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
     * 错误码,错误信息,原始异常
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

}