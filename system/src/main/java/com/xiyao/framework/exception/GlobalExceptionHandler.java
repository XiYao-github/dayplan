package com.xiyao.framework.exception;

import cn.hutool.core.util.StrUtil;
import com.xiyao.framework.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 *
 * @author xiyao
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 自动将返回结果转为 JSON
 * </p>
 *
 * <p>
 * 执行流程：
 * 1. Controller 抛出异常
 * 2. Spring 根据异常类型匹配对应的 @ExceptionHandler
 * 3. 执行对应方法，返回统一格式的 Result
 * </p>
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 认证授权异常（400-499） ====================

    /**
     * Spring Security 认证异常（未登录、登录失败等）
     * <p>
     * 常见子类：
     * - BadCredentialsException：用户名或密码错误
     * - LockedException：账户已锁定
     * - DisabledException：账户已禁用
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 认证失败: {}", requestURI, e.getMessage());

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
     * HTTP 状态码：403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 权限不足: {}", requestURI, e.getMessage());
        return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足，拒绝访问");
    }

    // ==================== 参数校验异常（400） ====================

    /**
     * @Valid 校验异常（POST/PUT 请求体校验）
     * <p>
     * 触发场景：Controller 方法参数使用 @Valid 注解
     * public Result addUser(@Valid @RequestBody UserDTO user)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(HttpStatus.BAD_REQUEST.value(), errorMsg);
    }

    /**
     * @Validated 分组校验异常（GET 请求参数校验）
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", errorMsg);
        return Result.error(HttpStatus.BAD_REQUEST.value(), errorMsg);
    }

    /**
     * 单个参数校验异常（如 @NotBlank 在 Controller 参数上）
     * public Result getList(@RequestParam @NotBlank String name)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", errorMsg);
        return Result.error(HttpStatus.BAD_REQUEST.value(), errorMsg);
    }

    /**
     * 缺少必要参数（@RequestParam 必填参数没传）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必要参数: {}", e.getParameterName());
        return Result.error(HttpStatus.BAD_REQUEST.value(),
                StrUtil.format("缺少必要参数: {}", e.getParameterName()));
    }

    /**
     * 参数类型不匹配（如 Integer 参数传了字符串 "abc"）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {} 应为 {}", e.getName(), e.getRequiredType());
        return Result.error(HttpStatus.BAD_REQUEST.value(),
                StrUtil.format("参数[{}]类型不匹配", e.getName()));
    }

    /**
     * 请求体格式错误（JSON 语法错误，或字段类型不匹配）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求体格式错误，请检查 JSON 格式");
    }

    // ==================== 业务异常 ====================


    // ==================== 系统异常（500） ====================

    /**
     * 运行时异常（未知异常，需要重点排查）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("运行时异常: 请求地址'{}', 异常信息", requestURI, e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后再试");
    }

    /**
     * 所有未捕获的异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("系统异常: 请求地址'{}', 异常类型: {}", requestURI, e.getClass().getName(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请联系管理员");
    }
}