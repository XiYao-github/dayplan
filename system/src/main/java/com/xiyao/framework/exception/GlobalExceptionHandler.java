package com.xiyao.framework.exception;

import com.xiyao.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthenticationException(AuthenticationException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 打印日志
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',认证失败,请重新登录.'{}'", requestURI, e.getMessage());
        // 返回错误信息
        Result error = Result.error(HttpStatus.UNAUTHORIZED.value(), "认证失败,请重新登录.");
        // 根据不同的子类返回不同的提示
        if (e instanceof BadCredentialsException) {
            error.setMsg("用户名或密码错误.");
            return error;
        }
        if (e instanceof LockedException) {
            error.setMsg("账户已锁定.");
            return error;
        }
        if (e instanceof DisabledException) {
            error.setMsg("账户已禁用.");
            return error;
        }
        return error;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限不足,拒绝访问.'{}'", requestURI, e.getMessage());
        return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足,拒绝访问.");
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return Result.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return Result.error(e.getMessage());
    }

}
