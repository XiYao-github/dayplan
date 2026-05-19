package com.xiyao.framework.exception;

import com.xiyao.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Spring Security 认证异常（未登录、登录失败等）
     * 常见子类：
     * - BadCredentialsException：用户名或密码错误
     * - LockedException：账户已锁定
     * - DisabledException：账户已禁用
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("请求地址'{}', 认证失败: {}", request.getRequestURI(), e.getMessage());

        if (e instanceof BadCredentialsException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }
        if (e instanceof LockedException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "账户已锁定，请联系管理员");
        }
        if (e instanceof DisabledException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "账户已禁用，请联系管理员");
        }
        return Result.error(HttpStatus.UNAUTHORIZED.value(), "认证失败，请重新登录");
    }

    /**
     * 权限不足异常（访问需要特定权限的接口）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("请求地址'{}', 权限不足: {}", request.getRequestURI(), e.getMessage());
        return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足，拒绝访问");
    }

    /**
     * 运行时异常（未知异常，需要重点排查）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: 请求地址'{}', 异常类型: {}", request.getRequestURI(), e.getClass().getName(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后再试");
    }

    /**
     * 系统未知异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: 请求地址'{}', 异常类型: {}", request.getRequestURI(), e.getClass().getName(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请联系管理员");
    }
}