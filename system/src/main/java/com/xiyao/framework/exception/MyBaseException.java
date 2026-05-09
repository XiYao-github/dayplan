package com.xiyao.framework.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * 自定义异常
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MyBaseException extends RuntimeException {

    /**
     * 失败
     */
    public static final int ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 编码
     */
    private Integer code;

    /**
     * 消息
     */
    private String msg;

    public MyBaseException(String msg) {
        this.code = ERROR;
        this.msg = msg;
    }

    public MyBaseException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
