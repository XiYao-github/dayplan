package com.xiyao.framework.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常类
 * <p>
 * 用于在 Service 层抛出业务逻辑相关的异常，
 * 由 GlobalExceptionHandler 统一处理并返回标准格式响应。
 * </p>
 *
 * <p>
 * HttpStatus 枚举说明：
 * Spring 提供的 HTTP 状态码标准枚举，包含所有标准状态码：
 * <ul>
 *   <li>HttpStatus.BAD_REQUEST（400）：参数错误</li>
 *   <li>HttpStatus.UNAUTHORIZED（401）：未认证</li>
 *   <li>HttpStatus.FORBIDDEN（403）：无权限</li>
 *   <li>HttpStatus.NOT_FOUND（404）：资源不存在</li>
 *   <li>HttpStatus.INTERNAL_SERVER_ERROR（500）：服务器错误</li>
 * </ul>
 * </p>
 *
 * @author xiyao
 * @since 1.0.0
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码（推荐使用 HttpStatus 枚举）
     * 如果有业务自定义错误码，可以使用 Integer
     */
    private final HttpStatus httpStatus;

    /**
     * 自定义错误码（可选，用于更细粒度的业务错误识别）
     */
    private final Integer customCode;

    /**
     * 错误消息
     */
    private final String msg;

    /**
     * 使用 HttpStatus 构造
     *
     * @param httpStatus HTTP 状态码
     * @param msg        错误消息
     */
    public BizException(HttpStatus httpStatus, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
        this.customCode = null;
        this.msg = msg;
    }

    /**
     * 使用自定义错误码构造
     *
     * @param customCode 自定义错误码（如 1001）
     * @param msg        错误消息
     */
    public BizException(Integer customCode, String msg) {
        super(msg);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;  // 默认 500
        this.customCode = customCode;
        this.msg = msg;
    }

    /**
     * 使用 HttpStatus + 自定义错误码
     *
     * @param httpStatus HTTP 状态码
     * @param customCode 自定义错误码
     * @param msg        错误消息
     */
    public BizException(HttpStatus httpStatus, Integer customCode, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
        this.customCode = customCode;
        this.msg = msg;
    }

    /**
     * 直接获取状态码（用于返回给前端）
     * 如果有自定义错误码，优先返回自定义码；否则返回 HTTP 状态码
     */
    public int getCode() {
        return customCode != null ? customCode : httpStatus.value();
    }
}