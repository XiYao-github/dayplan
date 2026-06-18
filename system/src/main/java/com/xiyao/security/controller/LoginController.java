package com.xiyao.security.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.data.Result;
import com.xiyao.dict.enums.Status;
import com.xiyao.common.base.exception.MyBaseException;
import com.xiyao.framework.utils.SpringUtils;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
 * <p>
 * <b>日志记录：</b>
 * 所有认证操作（登录、注册、退出）都会通过 Spring Event 机制发布 LogLoginEvent 事件，
 * 由 LogListener 异步保存到数据库，支持操作日志防篡改（SM3 哈希链）和全链路追踪（traceId）。
 *
 * @author xiyao
 * @see JwtUtils
 * @see UserVo
 * @see LogLoginEvent
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnBean(AuthenticationManager.class)
public class LoginController extends MyBaseController {

    /**
     * 认证管理器
     * <p>
     * Spring Security 核心组件，用于处理用户认证请求。
     * 认证过程中会调用 UserDetailsServiceImpl 加载用户信息进行验证。
     */
    private final AuthenticationManager authenticationManager;

    /**
     * 密码加密器
     * <p>
     * 使用 BCrypt 算法加密用户密码。
     * BCrypt 每次加密结果不同（自带随机盐），但验证时可匹配。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT 工具类
     * <p>
     * 用于生成和验证 JWT Token，以及操作用户信息的 Redis 缓存。
     */
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     * <p>
     * 接收用户名和密码，使用 Spring Security 进行认证。
     * 认证成功后生成 JWT Token 返回，失败时发布失败事件并抛出异常。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>接收 UserVo 登录参数</li>
     *     <li>构造 UsernamePasswordAuthenticationToken 认证令牌</li>
     *     <li>调用 AuthenticationManager.authenticate() 进行认证</li>
     *     <li>认证成功：生成 JWT Token，返回给前端</li>
     *     <li>认证失败：发布失败事件，抛出 MyBaseException</li>
     * </ol>
     *
     * <p>
     * <b>日志事件：</b>
     * 无论成功或失败，都会通过 LogLoginEvent 记录登录操作。
     * 成功事件包含用户 ID、用户名，失败事件包含失败原因。
     *
     * @param user 登录用户信息（包含用户名、密码）
     * @return 成功时返回 JWT Token，失败时抛出 MyBaseException
     * @throws MyBaseException 认证失败时抛出（用户名不存在、密码错误、用户被禁用等）
     */
    @PostMapping("/login")
    public Result<Object> login(@RequestBody UserVo user) {
        // 参数校验
        if (ObjectUtil.isNull(user)) {
            throw new MyBaseException("登录信息不能为空");
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            throw new MyBaseException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new MyBaseException("密码不能为空");
        }

        // 构造登录事件对象，用于记录登录日志
        // 事件会在 finally 块中发布，确保无论成功失败都记录
        LogLoginEvent event = new LogLoginEvent();
        try {
            // ========== 认证处理 ==========

            // 创建 UsernamePasswordAuthenticationToken 认证令牌
            // 参数说明：
            //   - principal: 用户标识（用户名）
            //   - credentials: 凭证（密码）
            // 认证时不设置 authorities，后续从 SecurityContext 获取
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // 调用 AuthenticationManager 进行认证
            // 如果认证失败（如密码错误、用户不存在），会抛出 AuthenticationException
            // 认证成功返回已填充的 Authentication 对象
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 认证成功，从 Authentication 对象中获取用户信息
            // LoginUser 实现了 UserDetails 接口，包含用户 ID、权限等信息
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 构造登录成功事件
            // 设置用户ID、用户名、认证类型、状态、消息、时间等
            event.setUserId(loginUser.getUserId())
                    .setUsername(loginUser.getUsername())
                    .setType(OperationType.LOGIN.ordinal())
                    .setStatus(OperationStatus.SUCCESS.ordinal())
                    .setMessage("登录成功")
                    .setTime(LocalDateTime.now());

            // 生成 JWT Token 并返回给前端
            // Token 包含用户登录标识，有效期内可复用
            return ok(jwtUtils.getToken(loginUser));

        } catch (Exception e) {
            // ========== 认证失败处理 ==========

            // 构造登录失败事件
            // 设置用户名（无法获取用户ID，因为用户可能不存在）
            // 设置失败状态和异常消息
            event.setUsername(user.getUsername())
                    .setType(OperationType.LOGIN.ordinal())
                    .setStatus(OperationStatus.FAIL.ordinal())
                    .setMessage(e.getMessage())
                    .setTime(LocalDateTime.now());

            // 抛出业务异常，由全局异常处理器统一处理
            // 异常信息会返回给前端展示
            throw new MyBaseException(e.getMessage(), e);

        } finally {
            // ========== 发布登录事件 ==========

            // 通过 Spring Event 机制发布事件
            // 事件会被 LogListener 异步监听，保存到数据库
            // LogListener 会计算 SM3 哈希链，保证日志防篡改
            SpringUtils.publishEvent(event);
        }
    }

    /**
     * 用户注册
     * <p>
     * 创建新用户账号，使用 BCrypt 算法加密密码，
     * 默认分配普通用户角色（roleId=2）。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>检查用户名是否已存在</li>
     *     <li>使用 BCrypt 加密密码</li>
     *     <li>保存用户信息到 sys_user 表</li>
     *     <li>关联默认角色到 sys_user_role 表</li>
     *     <li>发布注册成功/失败事件</li>
     * </ol>
     *
     * <p>
     * <b>注意事项：</b>
     * <ul>
     *     <li>默认分配普通用户角色（roleId=2），生产环境应指定角色</li>
     *     <li>密码使用 BCrypt 加密存储，同一密码每次加密结果不同</li>
     *     <li>用户名重复会返回错误，不会创建重复用户</li>
     * </ul>
     *
     * @param user 注册用户信息（包含用户名、密码）
     * @return 成功返回 null，失败抛出 MyBaseException
     * @throws MyBaseException 用户已存在或注册失败时抛出
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody UserVo user) {
        // 参数校验
        if (ObjectUtil.isNull(user)) {
            throw new MyBaseException("注册信息不能为空");
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            throw new MyBaseException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new MyBaseException("密码不能为空");
        }
        // 构造注册事件对象，用于记录注册日志
        LogLoginEvent event = new LogLoginEvent();
        try {
            // ========== 第1步：检查用户名是否已存在 ==========

            // 查询用户名是否存在（未删除的用户）
            Long count = Db.lambdaQuery(SysUser.class)
                    .eq(SysUser::getUsername, user.getUsername())  // 用户名匹配
                    .eq(SysUser::getDeleted, 0)                    // 未被删除
                    .count();                                      // 统计数量

            // 如果数量大于 0，说明用户名已存在
            if (count > 0) {
                // 构造失败事件（无法获取用户ID，因为还没创建成功）
                event.setUsername(user.getUsername())
                        .setType(OperationType.REGISTER.ordinal())
                        .setStatus(OperationStatus.FAIL.ordinal())
                        .setMessage("用户已存在")
                        .setTime(LocalDateTime.now());
                // 返回错误提示
                return error("用户已存在");
            }

            // ========== 第2步：创建新用户对象 ==========

            // 构造新用户对象
            SysUser newUser = new SysUser();
            newUser.setUsername(user.getUsername());  // 设置用户名

            // 使用 BCrypt 算法加密密码
            // BCrypt 会自动生成随机盐，每次加密结果不同
            // 验证时通过 BCryptPasswordEncoder.matches() 比对
            String encode = passwordEncoder.encode(user.getPassword());
            newUser.setPassword(encode);               // 设置加密后的密码

            newUser.setStatus(Status.NORMAL.getValue());

            // ========== 第3步：保存用户到数据库 ==========

            // 插入用户记录，获取自动生成的主键 ID
            Db.save(newUser);

            // ========== 第4步：分配默认角色 ==========

            // 构造用户-角色关联对象
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(newUser.getId());       // 关联用户 ID
            userRole.setRoleId(2L);                    // 关联普通用户角色 ID（roleId=2）

            // 保存用户-角色关联记录
            Db.save(userRole);

            // ========== 第5步：构造成功事件 ==========

            // 构造注册成功事件
            event.setUserId(newUser.getId())          // 设置用户 ID
                    .setUsername(newUser.getUsername()) // 设置用户名
                    .setType(OperationType.REGISTER.ordinal())  // 注册类型
                    .setStatus(OperationStatus.SUCCESS.ordinal())   // 成功状态
                    .setMessage("注册成功")             // 消息
                    .setTime(LocalDateTime.now()); // 注册时间

            // 返回成功
            return ok();

        } catch (Exception e) {
            // ========== 注册失败处理 ==========

            // 构造失败事件
            event.setUsername(user.getUsername())
                    .setType(OperationType.REGISTER.ordinal())
                    .setStatus(OperationStatus.FAIL.ordinal())
                    .setMessage(e.getMessage())
                    .setTime(LocalDateTime.now());

            // 抛出业务异常
            throw new MyBaseException(e.getMessage(), e);

        } finally {
            // ========== 发布注册事件 ==========

            // 通过 Spring Event 机制发布事件
            SpringUtils.publishEvent(event);
        }
    }

    /**
     * 用户退出登录
     * <p>
     * 从请求 Header 中获取 JWT Token，清除 Redis 中缓存的用户信息。
     * 无论退出成功或失败，都会发布相应事件。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>从请求 Header 中获取 JWT Token</li>
     *     <li>解析 Token 获取当前用户信息</li>
     *     <li>从 Redis 中删除用户缓存</li>
     *     <li>发布退出成功/失败事件</li>
     * </ol>
     *
     * <p>
     * <b>退出场景说明：</b>
     * <ul>
     *     <li>Token 有效：删除 Redis 缓存，返回成功</li>
     *     <li>Token 已过期：无法获取用户信息，抛出异常</li>
     *     <li>Token 无效：验证失败，抛出异常</li>
     * </ul>
     *
     * @param request HTTP 请求对象（用于从 Header 获取 Token）
     * @return 成功返回 null，失败抛出 MyBaseException
     * @throws MyBaseException Token 无效或已过期时抛出
     */
    @PostMapping("/logout")
    public Result<Object> logout(HttpServletRequest request) {
        // 构造登出事件对象，用于记录退出日志
        LogLoginEvent event = new LogLoginEvent();
        try {
            // ========== 第1步：获取 Token ==========

            // 从请求 Header 中获取 JWT Token
            // 格式：Authorization: Bearer {token}
            String token = jwtUtils.getHeaderToken(request);

            // ========== 第2步：获取当前用户信息 ==========

            // 解析 Token 获取登录用户信息
            // 如果 Token 无效或已过期，会抛出异常
            LoginUser loginUser = jwtUtils.getLoginUser(token);

            // ========== 第3步：构造成功事件 ==========

            // 构造退出成功事件
            event.setUserId(loginUser.getUserId())     // 设置用户 ID
                    .setUsername(loginUser.getUsername()) // 设置用户名
                    .setType(OperationType.LOGOUT.ordinal())  // 登出类型
                    .setStatus(OperationStatus.SUCCESS.ordinal())   // 成功状态
                    .setMessage("退出成功")             // 消息
                    .setTime(LocalDateTime.now()); // 退出时间

            // ========== 第4步：删除 Redis 缓存 ==========

            // 删除 Redis 中缓存的用户信息
            // removeToken 返回 true 表示删除成功
            // 返回 false 可能是因为用户已不在线（Token 已过期）
            return jwtUtils.removeToken(token) ? ok() : error("退出失败");

        } catch (Exception e) {
            // ========== 退出失败处理 ==========

            // 构造失败事件
            // 无法获取用户ID和用户名，因为 Token 无效
            event.setType(OperationType.LOGOUT.ordinal())
                    .setStatus(OperationStatus.FAIL.ordinal())
                    .setMessage(e.getMessage())
                    .setTime(LocalDateTime.now());

            // 抛出业务异常
            throw new MyBaseException(e.getMessage(), e);

        } finally {
            // ========== 发布登出事件 ==========

            // 通过 Spring Event 机制发布事件
            SpringUtils.publishEvent(event);
        }
    }

}