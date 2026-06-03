package com.xiyao.framework.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.xiyao.common.utils.data.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一捕获并处理 Controller 层抛出的所有异常，
 * 转换为标准的 Result 响应格式返回给前端。
 *
 * <p>
 * <b>异常分类（共8大类37个处理器）：</b>
 * <ol>
 *     <li>Web 参数校验异常（5个）：@Valid 校验、表单绑定、单参数校验等</li>
 *     <li>Web 请求格式异常（5个）：JSON 解析、请求方法、媒体类型等</li>
 *     <li>Spring Security 异常（2个）：认证失败、权限不足</li>
 *     <li>MySQL 数据库异常（8个）：唯一键、外键、超时、语法错误等</li>
 *     <li>Redis 异常（1个）：连接失败</li>
 *     <li>Jackson 异常（2个）：JSON 解析、映射错误</li>
 *     <li>业务异常（1个）：BusinessException 自定义</li>
 *     <li>兜底异常（2个）：RuntimeException、Exception</li>
 * </ol>
 *
 * <p>
 * <b>配合说明：</b>
 * 此处理器与 {@link Result} 统一响应配合使用，
 * 所有异常最终都返回 {code, msg, data, traceId} 结构。
 *
 * @author xiyao
 * @see Result
 * @see BusinessException
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 1. Web 参数校验异常 ====================

    /**
     * 处理 @Valid 校验异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>Controller 方法参数使用 @Valid 注解</li>
     *   <li>请求体 JSON 中的字段不满足校验注解约束（如 @NotNull、@Size、@Pattern 等）</li>
     *   <li>例如：用户注册时用户名长度为3（要求4-20位）</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：根据返回的错误字段信息，高亮对应输入框并提示错误原因</li>
     *   <li>后端：记录校验失败的字段和原因，便于排查接口调用问题</li>
     * </ul>
     *
     * <p>返回示例：</p>
     * <pre>{@code
     * {
     *   "code": 400,
     *   "msg": "name: 姓名不能为空; age: 年龄必须在1-120之间"
     * }
     * }</pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理表单绑定异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>使用 @ModelAttribute 接收表单参数</li>
     *   <li>表单参数类型转换失败（如 String 转 Date 失败）</li>
     *   <li>表单参数不满足校验注解约束</li>
     *   <li>例如：提交表单时 age 字段传了 "abc"（期望数字）</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查表单字段名称是否正确，数据类型是否匹配</li>
     *   <li>后端：确认 @ModelAttribute 参数名与表单字段名一致</li>
     * </ul>
     */
    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理单参数校验异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>Controller 方法参数直接使用校验注解（如 @RequestParam @NotBlank）</li>
     *   <li>类上使用 @Validated 注解</li>
     *   <li>例如：@RequestParam @Min(1) Integer pageNum 传入 0</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查请求参数是否符合约束条件</li>
     *   <li>后端：确认校验注解的参数值设置合理</li>
     * </ul>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理缺少必填参数异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>请求缺少必需的 Query 参数（@RequestParam(required = true)）</li>
     *   <li>请求缺少必需的路径参数（@PathVariable）</li>
     *   <li>例如：接口需要 ?id=1，但请求没有传 id 参数</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查接口文档，补全所有必填参数</li>
     *   <li>后端：考虑将 required 设为 false 并提供默认值</li>
     * </ul>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数: {}", e.getParameterName());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "缺少必填参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>请求参数类型与 Controller 方法参数类型不一致</li>
     *   <li>例如：接口期望 Integer 类型的 age，但传了字符串 "abc"</li>
     *   <li>例如：接口期望 Date 类型，但传了格式不正确的日期字符串</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查参数值是否符合接口定义的数据类型</li>
     *   <li>后端：可使用 @DateTimeFormat 注解指定日期格式</li>
     * </ul>
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(), e.getRequiredType().getSimpleName());
        log.warn("参数类型不匹配: {}", message);
        return Result.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    // ==================== 2. Web 请求格式异常 ====================

    /**
     * 处理请求体格式错误异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>请求体 JSON 格式不正确（缺少括号、引号不匹配、多余逗号等）</li>
     *   <li>JSON 字段类型与实体类字段类型不匹配</li>
     *   <li>例如：{"name": "张三", "age": "不是数字"} 而 age 是 Integer 类型</li>
     *   <li>例如：{"name": "张三", age: 25}（age 缺少双引号）</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：使用 JSON 验证工具检查格式，确保字段名用双引号包裹</li>
     *   <li>后端：确认实体类字段类型与 JSON 数据类型匹配</li>
     * </ul>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());

        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof JsonParseException) {
            return Result.error(HttpStatus.BAD_REQUEST.value(), "JSON格式错误，请检查括号、引号是否正确");
        }
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            return Result.error(HttpStatus.BAD_REQUEST.value(),
                    String.format("字段 '%s' 类型错误，期望类型: %s",
                            ife.getPath().get(0).getFieldName(),
                            ife.getTargetType().getSimpleName()));
        }
        if (cause instanceof JsonMappingException) {
            return Result.error(HttpStatus.BAD_REQUEST.value(), "JSON字段映射错误，请检查字段类型");
        }
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求体格式错误，请检查JSON格式");
    }

    /**
     * 处理请求方法不支持异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>使用错误的 HTTP 方法请求接口</li>
     *   <li>例如：接口是 POST /user，但用 GET 请求</li>
     *   <li>例如：接口是 DELETE /user/{id}，但用 POST 请求</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查接口文档，使用正确的 HTTP 方法</li>
     *   <li>后端：确认 @RequestMapping 或 @XxxMapping 注解的 method 属性</li>
     * </ul>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED.value(),
                String.format("请求方法 '%s' 不支持", e.getMethod()));
    }

    /**
     * 处理媒体类型不支持异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>请求的 Content-Type 与接口期望的不一致</li>
     *   <li>例如：接口使用 @PostMapping(consumes = "application/json")，但请求 Content-Type 是 text/plain</li>
     *   <li>例如：文件上传接口 expected multipart/form-data 但传了 application/json</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：设置正确的 Content-Type 请求头</li>
     *   <li>后端：确认 consumes 属性配置是否合理</li>
     * </ul>
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("媒体类型不支持: {}", e.getMessage());
        return Result.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "不支持的媒体类型，请使用 application/json");
    }

    /**
     * 处理接口不存在异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>请求的 URL 路径没有对应的 Controller 映射</li>
     *   <li>需要在 application.yml 中配置 spring.mvc.throw-exception-if-no-handler-found=true</li>
     *   <li>例如：请求 /api/user/info123，但实际接口是 /api/user/info</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查接口 URL 是否正确</li>
     *   <li>后端：确认 @RequestMapping 路径是否正确</li>
     * </ul>
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(HttpStatus.NOT_FOUND.value(), "接口不存在: " + e.getRequestURL());
    }

    // ==================== 3. Spring Security 异常 ====================

    /**
     * 处理认证异常（未登录、登录失败等）
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>未携带 Token 或 Token 已过期 -> AuthenticationCredentialsNotFoundException</li>
     *   <li>用户名或密码错误 -> BadCredentialsException</li>
     *   <li>账户被锁定 -> LockedException</li>
     *   <li>账户被禁用 -> DisabledException</li>
     *   <li>账户已过期 -> AccountExpiredException</li>
     *   <li>密码已过期 -> CredentialsExpiredException</li>
     *   <li>认证信息不足（如需要两步验证）-> InsufficientAuthenticationException</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：401 时清除本地 Token，跳转到登录页</li>
     *   <li>前端：根据错误信息提示用户具体操作（重新登录、修改密码、联系管理员等）</li>
     *   <li>后端：记录登录失败次数，达到阈值锁定账户</li>
     * </ul>
     *
     * <p>返回示例：</p>
     * <pre>{@code
     * // 未登录
     * {"code": 401, "msg": "请先登录"}
     * // 密码错误
     * {"code": 401, "msg": "用户名或密码错误"}
     * // 账户锁定
     * {"code": 401, "msg": "账户已锁定，请联系管理员"}
     * }</pre>
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Object> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getClass().getSimpleName());

        if (e instanceof AuthenticationCredentialsNotFoundException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (e instanceof InsufficientAuthenticationException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "认证信息不足，请重新登录");
        }
        if (e instanceof BadCredentialsException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }
        if (e instanceof UsernameNotFoundException) {
            // 统一提示，避免信息泄露
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }
        if (e instanceof LockedException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "账户已锁定，请联系管理员");
        }
        if (e instanceof DisabledException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "账户已禁用，请联系管理员");
        }
        if (e instanceof AccountExpiredException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "账户已过期");
        }
        if (e instanceof CredentialsExpiredException) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "密码已过期，请修改密码");
        }
        return Result.error(HttpStatus.UNAUTHORIZED.value(), "认证失败，请重新登录");
    }

    /**
     * 处理权限不足异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>用户已登录但访问了超出其权限范围的接口</li>
     *   <li>例如：普通用户调用 @PreAuthorize("hasRole('ADMIN')") 的接口</li>
     *   <li>例如：用户只有读取权限，但尝试执行删除操作</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：捕获 403 错误，提示用户无权限，可隐藏无权限的按钮</li>
     *   <li>后端：检查 @PreAuthorize 或 @Secured 注解的权限表达式是否正确</li>
     * </ul>
     *
     * <p>返回示例：</p>
     * <pre>{@code
     * {"code": 403, "msg": "权限不足，拒绝访问"}
     * }</pre>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足，拒绝访问");
    }

    // ==================== 4. MyBatis-Plus / MySQL 数据库异常 ====================

    /**
     * 处理唯一键冲突异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>插入或更新数据时违反唯一索引约束</li>
     *   <li>例如：注册时用户名已存在</li>
     *   <li>例如：添加好友时好友关系已存在</li>
     *   <li>MySQL 错误码：1062</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：提示用户数据已存在，建议修改后重试</li>
     *   <li>后端：操作前先查询是否存在，或使用 ON DUPLICATE KEY UPDATE</li>
     * </ul>
     *
     * <p>返回示例：</p>
     * <pre>{@code
     * {"code": 409, "msg": "数据已存在，请勿重复提交"}
     * }</pre>
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Object> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        return Result.error(HttpStatus.CONFLICT.value(), "数据已存在，请勿重复提交");
    }

    /**
     * 处理数据完整性约束违反异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>外键约束：删除被其他表引用的数据（MySQL 错误码：1451）</li>
     *   <li>外键约束：添加数据时关联的外键不存在（MySQL 错误码：1452）</li>
     *   <li>非空约束：插入 NULL 到 NOT NULL 字段（MySQL 错误码：1048）</li>
     *   <li>数据过长：字段长度超限（MySQL 错误码：1406）</li>
     *   <li>类型不匹配：插入值与字段类型不符</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：根据错误提示修改输入或操作方式</li>
     *   <li>后端：删除前检查是否有关联数据，或使用软删除</li>
     *   <li>后端：在实体类使用 @TableField 注解指定字段长度</li>
     * </ul>
     *
     * <p>返回示例：</p>
     * <pre>{@code
     * {"code": 409, "msg": "数据被其他表引用，无法删除"}
     * {"code": 400, "msg": "请填写所有必填项"}
     * {"code": 400, "msg": "输入内容过长，请缩短后再试"}
     * }</pre>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMessage());

        Throwable cause = e.getMostSpecificCause();
        String message = cause.getMessage();

        if (message != null) {
            if (message.contains("foreign key") || message.contains("Cannot delete")) {
                return Result.error(HttpStatus.CONFLICT.value(), "数据被其他表引用，无法删除");
            }
            if (message.contains("cannot be null")) {
                return Result.error(HttpStatus.BAD_REQUEST.value(), "请填写所有必填项");
            }
            if (message.contains("Data too long")) {
                return Result.error(HttpStatus.BAD_REQUEST.value(), "输入内容过长，请缩短后再试");
            }
        }
        return Result.error(HttpStatus.CONFLICT.value(), "数据操作违反约束，请检查输入");
    }

    /**
     * 处理查询超时异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>SQL 执行时间超过 mybatis-plus.configuration.default-statement-timeout 设置的超时时间</li>
     *   <li>表数据量过大，缺少合适的索引</li>
     *   <li>复杂查询（如多表 JOIN、子查询）性能差</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：提示用户稍后重试</li>
     *   <li>后端：优化 SQL，添加索引，拆分复杂查询，使用分页</li>
     *   <li>后端：考虑使用异步处理或缓存</li>
     * </ul>
     */
    @ExceptionHandler(QueryTimeoutException.class)
    public Result<Object> handleQueryTimeoutException(QueryTimeoutException e) {
        log.error("查询超时: {}", e.getMessage(), e);
        return Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "查询超时，请稍后再试");
    }

    /**
     * 处理 SQL 语法错误异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>SQL 语法错误（MySQL 错误码：1064）</li>
     *   <li>表不存在（MySQL 错误码：1146）</li>
     *   <li>列不存在（MySQL 错误码：1054）</li>
     *   <li>多由 MyBatis-Plus 自动生成 SQL 异常或 XML 编写错误引起</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>后端：检查实体类与数据库表字段是否一致</li>
     *   <li>后端：检查 XML 中 SQL 语法</li>
     *   <li>后端：开启 MyBatis-Plus SQL 日志打印 SQL 排查</li>
     * </ul>
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    public Result<Object> handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        log.error("SQL语法错误: {}", e.getMessage(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统配置错误，请联系管理员");
    }

    /**
     * 处理数据库连接失败异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>数据库服务未启动</li>
     *   <li>网络不通或防火墙拦截</li>
     *   <li>连接池配置错误或连接数已满</li>
     *   <li>MySQL 错误码：2003、1040</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：提示用户稍后重试</li>
     *   <li>后端：检查数据库服务状态、连接配置、网络连通性</li>
     *   <li>后端：配置连接池的验证查询（validationQuery）</li>
     * </ul>
     */
    @ExceptionHandler(SQLNonTransientConnectionException.class)
    public Result<Object> handleSQLNonTransientConnectionException(SQLNonTransientConnectionException e) {
        log.error("数据库连接失败: {}", e.getMessage(), e);
        return Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "数据库服务不可用，请稍后再试");
    }

    /**
     * 处理 SQL 约束违反异常（MySQL 原生）
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>唯一键冲突（MySQL 错误码：1062）</li>
     *   <li>外键约束违反（MySQL 错误码：1451、1452）</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>唯一键冲突：提示用户数据已存在</li>
     *   <li>外键约束：提示关联数据不存在或被引用</li>
     * </ul>
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Object> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.warn("SQL约束违反: {}", e.getMessage());

        String message = e.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return Result.error(HttpStatus.CONFLICT.value(), "数据已存在");
        }
        return Result.error(HttpStatus.CONFLICT.value(), "数据操作失败，请检查输入");
    }

    /**
     * 处理 SQL 通用异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>其他未分类的 SQL 异常</li>
     *   <li>根据 MySQL 错误码返回友好提示</li>
     * </ul>
     *
     * <p>错误码说明：</p>
     * <ul>
     *   <li>1062：唯一键冲突 → 数据已存在</li>
     *   <li>1451：外键约束 → 数据被引用无法删除</li>
     *   <li>1452：外键约束 → 关联数据不存在</li>
     *   <li>1205：锁等待超时 → 操作等待超时，请稍后再试</li>
     *   <li>1213：死锁 → 操作冲突，请稍后重试</li>
     * </ul>
     */
    @ExceptionHandler(SQLException.class)
    public Result<Object> handleSQLException(SQLException e) {
        log.error("SQL异常: errorCode={}, sqlState={}, message={}",
                e.getErrorCode(), e.getSQLState(), e.getMessage(), e);

        int errorCode = e.getErrorCode();
        switch (errorCode) {
            case 1062:
                return Result.error(HttpStatus.CONFLICT.value(), "数据已存在");
            case 1451:
                return Result.error(HttpStatus.CONFLICT.value(), "数据被其他表引用，无法删除");
            case 1452:
                return Result.error(HttpStatus.CONFLICT.value(), "关联数据不存在");
            case 1205:
                return Result.error(HttpStatus.CONFLICT.value(), "操作等待超时，请稍后再试");
            case 1213:
                return Result.error(HttpStatus.CONFLICT.value(), "操作冲突，请稍后重试");
            default:
                return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据操作失败");
        }
    }

    /**
     * 处理数据访问通用异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>数据库访问失败的通用父类异常</li>
     *   <li>其他具体数据库异常未匹配时的兜底</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>记录详细日志便于排查</li>
     *   <li>返回通用错误提示</li>
     * </ul>
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Object> handleDataAccessException(DataAccessException e) {
        log.error("数据访问异常: {}", e.getMessage(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据库服务异常，请稍后再试");
    }

    // ==================== 5. Redis 异常 ====================

    /**
     * 处理 Redis 连接失败异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>Redis 服务未启动</li>
     *   <li>Redis 网络不通或防火墙拦截</li>
     *   <li>Redis 密码错误</li>
     *   <li>连接池配置错误</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：提示用户稍后重试</li>
     *   <li>后端：检查 Redis 服务状态、配置信息</li>
     *   <li>后端：实现降级方案（如直接查数据库）</li>
     * </ul>
     */
    @ExceptionHandler(RedisConnectionFailureException.class)
    public Result<Object> handleRedisConnectionFailureException(RedisConnectionFailureException e) {
        log.error("Redis连接失败: {}", e.getMessage(), e);
        return Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "缓存服务暂时不可用");
    }

    // ==================== 6. Jackson 异常 ====================

    /**
     * 处理 JSON 解析异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>JSON 字符串语法错误（缺少括号、引号不匹配、多余逗号等）</li>
     *   <li>例如：{"name": "张三", age: 25}（age 缺少双引号）</li>
     *   <li>例如：{"name": "张三",}（多余逗号）</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：使用 JSON.stringify() 序列化对象，确保格式正确</li>
     *   <li>前端：使用 JSON 验证工具检查格式</li>
     *   <li>后端：返回具体的 JSON 错误位置信息（开发环境）</li>
     * </ul>
     */
    @ExceptionHandler(JsonParseException.class)
    public Result<Object> handleJsonParseException(JsonParseException e) {
        log.warn("JSON解析错误: {}", e.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "JSON格式错误，请检查语法");
    }

    /**
     * 处理 JSON 映射异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>JSON 字段类型与 Java 实体类字段类型不匹配</li>
     *   <li>例如：age 期望 Integer，但传了 "abc"</li>
     *   <li>例如：createTime 期望 Date，但传了格式不正确的字符串</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：检查请求体中字段类型是否与接口定义一致</li>
     *   <li>后端：可使用 @JsonFormat 注解指定日期格式</li>
     * </ul>
     */
    @ExceptionHandler(JsonMappingException.class)
    public Result<Object> handleJsonMappingException(JsonMappingException e) {
        log.warn("JSON映射错误: {}", e.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "JSON字段类型不匹配");
    }

    // ==================== 7. 自定义业务异常 ====================

    /**
     * 处理自定义业务异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>业务逻辑校验失败时主动抛出</li>
     *   <li>例如：余额不足时抛出 BusinessException("余额不足")</li>
     *   <li>例如：订单状态不正确时抛出 BusinessException(4001, "订单不可取消")</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：根据返回的错误码和提示信息进行相应处理</li>
     *   <li>后端：使用 BusinessException 统一业务异常，便于前端统一处理</li>
     *   <li>后端：定义 ResultCode 枚举管理错误码</li>
     * </ul>
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 8. 兜底异常 ====================

    /**
     * 处理运行时异常
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>空指针异常（NullPointerException）</li>
     *   <li>数组越界异常（IndexOutOfBoundsException）</li>
     *   <li>非法参数异常（IllegalArgumentException）</li>
     *   <li>其他未捕获的运行时异常</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>前端：提示系统繁忙，稍后重试</li>
     *   <li>后端：</li>
     *   <li>  - 开发环境：返回详细错误信息便于调试</li>
     *   <li>  - 生产环境：返回通用提示，记录完整日志</li>
     *   <li>  - 重点排查空指针和边界条件问题</li>
     * </ul>
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: url={}, class={}, message={}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);

        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统错误: " + e.getMessage());
        // return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后再试");
    }

    /**
     * 处理系统顶级异常（兜底）
     *
     * <p>出现情况：</p>
     * <ul>
     *   <li>所有其他异常处理器的最后一道防线</li>
     *   <li>理论上不应该走到这里，作为最后保障</li>
     * </ul>
     *
     * <p>处理建议：</p>
     * <ul>
     *   <li>记录完整错误日志</li>
     *   <li>通知开发人员排查</li>
     *   <li>返回通用错误提示</li>
     * </ul>
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: url={}, class={}, message={}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);

        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常: " + e.getMessage());
        // return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请联系管理员");
    }
}