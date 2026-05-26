package com.xiyao.security.controller;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.log.enums.OperationStatus;
import com.xiyao.log.enums.OperationType;
import com.xiyao.log.event.LogLoginEvent;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.details.UserVo;
import com.xiyao.security.utils.JwtUtils;
import com.xiyao.system.entity.SysUser;
import com.xiyao.system.entity.SysUserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 登录控制器
 * <p>
 * 处理用户认证相关请求，包括登录、注册、退出等操作。
 * 使用 Spring Security 的 AuthenticationManager 进行用户认证，
 * 认证成功后生成 JWT Token 返回给客户端。
 *
 * <p>
 * <b>功能说明：</b>
 * <ul>
 *     <li>用户登录：验证用户名密码，认证成功后返回 JWT Token</li>
 *     <li>用户注册：创建新用户账号，默认分配普通用户角色</li>
 *     <li>用户退出：清除 Redis 中缓存的用户信息</li>
 * </ul>
 *
 * <p>
 * <b>认证流程：</b>
 * <ol>
 *     <li>用户提交用户名和密码到 /login 接口</li>
 *     <li>使用 AuthenticationManager 进行认证</li>
 *     <li>认证成功后查询用户信息和权限</li>
 *     <li>生成 JWT Token 返回给客户端</li>
 *     <li>客户端后续请求携带 Token 进行身份验证</li>
 * </ol>
 *
 * @author xiyao
 * @see JwtUtils
 * @see UserVo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnBean(AuthenticationManager.class)
public class LoginController extends MyBaseController {

    /**
     * 认证管理器
     * <p>
     * 用于处理用户认证请求，验证用户名密码是否正确。
     */
    private final AuthenticationManager authenticationManager;

    /**
     * 密码加密器
     * <p>
     * 使用 BCrypt 算法加密用户密码。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT 工具类
     * <p>
     * 用于生成和验证 JWT Token。
     */
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     * <p>
     * 接收用户名和密码，使用 Spring Security 进行认证。
     * 认证成功后生成 JWT Token 返回，失败则抛出异常。
     *
     * @param user 登录用户信息（包含用户名、密码、验证码）
     * @return 成功时返回 JWT Token，失败时返回错误信息
     * @throws BusinessException 认证失败时抛出（用户名不存在、密码错误、用户被禁用等）
     * @see UserVo
     * @see JwtUtils#getToken(LoginUser)
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserVo user) {
        // 获取用户名和密码
        String username = user.getUsername();
        String password = user.getPassword();

        // 构造登录事件对象，用于记录登录日志
        LogLoginEvent event = new LogLoginEvent();
        try {
            // 创建 UsernamePasswordAuthenticationToken 对象
            // 参数：principal（用户标识）、credentials（凭证/密码）
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // 使用 AuthenticationManager 进行认证
            // 如果认证失败会抛出 AuthenticationException
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 认证成功，获取用户信息（LoginUser 实现 UserDetails 接口）
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 构造登录成功事件
            event.setUserId(loginUser.getUserId())
                    .setUsername(loginUser.getUsername())
                    .setAuthType(OperationType.LOGIN.ordinal())
                    .setStatus(OperationStatus.SUCCESS.ordinal())
                    .setMessage("登录成功")
                    .setLoginTime(LocalDateTime.now())
                    .setTraceId(MDC.get("traceId"));

            // 发布登录成功事件（用于日志记录等后续操作）
            SpringUtil.publishEvent(event);

            // 生成 JWT Token 并返回
            return ok(jwtUtils.getToken(loginUser));

        } catch (Exception e) {
            // 认证失败，构造登录失败事件
            event.setUsername(user.getUsername())
                    .setAuthType(OperationType.LOGIN.ordinal())
                    .setStatus(OperationStatus.FAIL.ordinal())
                    .setMessage(e.getMessage())
                    .setLoginTime(LocalDateTime.now())
                    .setTraceId(MDC.get("traceId"));

            // 发布登录失败事件
            SpringUtil.publishEvent(event);

            // 抛出业务异常，由全局异常处理器统一处理
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * 用户注册
     * <p>
     * 创建新用户账号，使用 BCrypt 算法加密密码，
     * 默认分配普通用户角色（roleId=2）。
     *
     * @param user 注册用户信息（包含用户名、密码）
     * @return 成功返回 null，失败返回错误信息
     * @see PasswordEncoder#encode(CharSequence)
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserVo user) {
        // 检查用户名是否已存在
        Long count = Db.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, user.getUsername())
                .count();

        // 用户名已存在，直接返回错误
        if (count > 0) {
            return error("用户已存在");
        }

        // 构造新用户对象
        SysUser newUser = new SysUser();
        newUser.setUsername(user.getUsername());

        // 使用 BCrypt 算法加密密码
        // BCrypt 每次加密结果不同，但可以验证
        String encode = passwordEncoder.encode(user.getPassword());
        newUser.setPassword(encode);
        newUser.setStatus(1);  // 启用状态
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());

        // 保存用户到数据库
        Db.save(newUser);

        // 默认分配普通用户角色（测试用，生产环境应指定角色）
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(2L);  // 普通用户角色 ID
        Db.save(userRole);

        // 构造注册成功事件并发布
        LogLoginEvent event = new LogLoginEvent();
        event.setUserId(newUser.getId())
                .setUsername(newUser.getUsername())
                .setAuthType(OperationType.REGISTER.ordinal())
                .setStatus(OperationStatus.SUCCESS.ordinal())
                .setMessage("注册成功")
                .setLoginTime(LocalDateTime.now())
                .setTraceId(MDC.get("traceId"));
        SpringUtil.publishEvent(event);

        return ok();
    }

    /**
     * 用户退出登录
     * <p>
     * 从请求 Header 中获取 JWT Token，
     * 清除 Redis 中缓存的用户信息。
     *
     * @param request HTTP 请求（用于获取 Token）
     * @return 成功返回 null，失败返回错误信息
     * @see JwtUtils#getHeaderToken(HttpServletRequest)
     * @see JwtUtils#removeToken(String)
     */
    @DeleteMapping("/logout")
    public Result logout(HttpServletRequest request) {
        // 从请求 Header 中获取 JWT Token（Bearer Token 格式）
        String token = jwtUtils.getHeaderToken(request);

        // 获取当前登录用户信息
        LoginUser loginUser = jwtUtils.getLoginUser(token);

        // 构造登出事件并发布
        LogLoginEvent event = new LogLoginEvent();
        event.setUserId(loginUser.getUserId())
                .setUsername(loginUser.getUsername())
                .setAuthType(OperationType.LOGOUT.ordinal())
                .setStatus(OperationStatus.SUCCESS.ordinal())
                .setMessage("退出成功")
                .setLoginTime(LocalDateTime.now())
                .setTraceId(MDC.get("traceId"));
        SpringUtil.publishEvent(event);

        // 删除 Redis 中缓存的用户信息
        // 如果 Token 有效且删除成功返回 true，否则返回 false
        return jwtUtils.removeToken(token) ? ok() : error("退出失败");
    }
}