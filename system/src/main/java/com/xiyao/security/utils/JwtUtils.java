package com.xiyao.security.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.xiyao.framework.utils.RedisUtils;
import com.xiyao.security.details.LoginUser;
import com.xiyao.security.properties.SecurityData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 提供 JWT Token 的生成、验证、解析等功能，
 * 用于前后端分离架构的无状态身份认证。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>生成 Token：根据用户信息生成 JWT 令牌</li>
 *     <li>验证 Token：校验签名和有效期</li>
 *     <li>解析 Token：提取用户信息和登录标识</li>
 *     <li>缓存管理：用户信息 Redis 缓存和 Token 黑名单</li>
 * </ul>
 *
 * <p>
 * <b>Token 结构：</b>
 * <ul>
 *     <li>Header：包含签名算法</li>
 *     <li>Payload：包含用户登录标识（loginUserKey）</li>
 *     <li>Signature：使用密钥签名的哈希值</li>
 * </ul>
 *
 * <p>
 * <b>Redis 存储结构：</b>
 * <ul>
 *     <li>Key：login_user_key:{uuid}</li>
 *     <li>Value：LoginUser 对象（包含用户信息和权限）</li>
 *     <li>TTL：配置的过期时间</li>
 * </ul>
 *
 * @author xiyao
 * @see LoginUser
 * @see SecurityData
 */
@Slf4j
@AllArgsConstructor
public class JwtUtils {

    /**
     * Redis 工具类
     * <p>
     * 用于存储和获取登录用户信息。
     */
    private final RedisUtils redisUtils;

    /**
     * Security 配置属性
     * <p>
     * 包含 JWT 密钥和过期时间配置。
     */
    private final SecurityData properties;

    // ==================== 常量定义 ====================

    /**
     * 登录令牌 Payload 中的键名
     * <p>
     * 用于在 JWT Payload 中存储用户登录标识的 key。
     */
    public static final String LOGIN_TOKEN = "login_token";

    /**
     * Redis 用户信息键前缀
     * <p>
     * 完整格式：login_user_key:{uuid}
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * Authorization 请求头前缀
     * <p>
     * 完整格式：Bearer {token}
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization 请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // ==================== 密钥和过期时间 ====================

    /**
     * 获取 JWT 签名密钥
     * <p>
     * 将配置中的密钥字符串转换为字节数组，用于 Token 签名和验证。
     *
     * @return UTF-8 编码的密钥字节数组
     */
    private byte[] getSecret() {
        // 从配置获取密钥并转换为 UTF-8 字节数组
        return properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取 Token 过期时间（秒）
     *
     * @return 过期时间秒数
     */
    private Long getSeconds() {
        // 从配置获取过期时间（秒）
        return properties.getJwt().getExpire();
    }

    /**
     * 获取 Token 过期时间（毫秒）
     *
     * @return 过期时间毫秒数
     */
    private Long getMilliseconds() {
        // 秒转毫秒
        return properties.getJwt().getExpire() * 1000;
    }

    // ==================== Token 操作 ====================

    /**
     * 生成 Token 并缓存用户信息
     * <p>
     * 为登录用户生成 JWT Token，并将用户信息存入 Redis 缓存。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>生成随机 UUID 作为登录用户标识</li>
     *     <li>将用户信息存入 Redis，key 为 login_user_key:uuid</li>
     *     <li>生成包含用户标识的 JWT Token 返回</li>
     * </ol>
     *
     * @param loginUser 登录用户信息（包含用户 ID、权限等）
     * @return JWT Token 字符串
     */
    public String getToken(LoginUser loginUser) {
        // 生成随机 UUID 作为登录用户 key
        String loginUserKey = IdUtil.fastSimpleUUID();
        // 将登录用户信息存入 Redis，设置过期时间
        // Key 格式：login_user_key:{uuid}
        // Value：LoginUser 对象（序列化为 JSON）
        redisUtils.set(LOGIN_USER_KEY + ":" + loginUserKey, loginUser, getSeconds());
        // 生成并返回 JWT Token
        return generateToken(loginUserKey);
    }

    /**
     * 生成 JWT Token
     * <p>
     * 根据登录用户标识生成 JWT 令牌。
     *
     * <p>
     * <b>Token 组成：</b>
     * <ul>
     *     <li>Payload.login_token：用户登录标识（UUID）</li>
     *     <li>iat：签发时间</li>
     *     <li>exp：过期时间</li>
     * </ul>
     *
     * @param loginUserKey 登录用户标识（UUID）
     * @return JWT Token 字符串
     */
    public String generateToken(String loginUserKey) {
        // 创建 Payload 数据
        Map<String, Object> map = new HashMap<>();
        // 存储用户登录标识
        map.put(LOGIN_TOKEN, loginUserKey);

        // 获取当前时间
        Date now = new Date();
        // 计算过期时间 = 当前时间 + 过期毫秒数
        Date expiration = new Date(now.getTime() + getMilliseconds());

        // 构建 JWT Token
        return JWT.create()
                .addPayloads(map)           // 设置 Payload 数据（用户标识）
                .setIssuedAt(now)           // 设置签发时间（iat）
                .setExpiresAt(expiration)   // 设置过期时间（exp）
                .setKey(getSecret())        // 设置签名密钥
                .sign();                    // 生成签名并返回
    }

    /**
     * 从请求头获取 Token
     * <p>
     * 从 HttpServletRequest 的 Authorization 请求头中提取 Token。
     * 支持 Bearer Token 格式。
     *
     * <p>
     * <b>请求头格式：</b>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     *
     * @param request HTTP 请求对象
     * @return Token 字符串（不含 Bearer 前缀），不存在返回 null
     */
    public String getHeaderToken(HttpServletRequest request) {
        // 获取 Authorization 请求头
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // 检查是否存在且为 Bearer Token 格式
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            // 去掉 "Bearer " 前缀并返回
            return bearerToken.replace(TOKEN_PREFIX, "");
        }
        // 非 Bearer Token 格式或为空，直接返回原始值
        return bearerToken;
    }

    /**
     * 验证 Token 有效性
     * <p>
     * 校验 JWT Token 的签名和有效期。
     *
     * <p>
     * <b>验证项：</b>
     * <ul>
     *     <li>签名验证：使用密钥验证 Token 是否被篡改</li>
     *     <li>有效期验证：检查 Token 是否已过期</li>
     * </ul>
     *
     * @param token JWT Token 字符串
     * @return true 验证通过，false 验证失败（签名错误或已过期）
     */
    public boolean validateToken(String token) {
        try {
            // 解析 Token
            JWT jwt = JWT.of(token).setKey(getSecret());
            // 验证签名（verify）和有效期（validate）
            // jwt.verify() 验证签名
            // jwt.validate(0) 验证过期时间，0 表示允许 0 秒的误差
            return jwt.verify() && jwt.validate(0);
        } catch (Exception e) {
            // 验证失败记录日志并返回 false
            log.debug("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取登录用户标识
     * <p>
     * 解析 JWT Token，提取 Payload 中的用户登录标识。
     *
     * @param token JWT Token 字符串
     * @return 登录用户标识（UUID）
     */
    public String getLoginUserKey(String token) {
        // 解析 Token 获取 Payload
        JWT jwt = JWTUtil.parseToken(token);
        // 从 Payload 中获取 login_token 键的值
        return (String) jwt.getPayload(LOGIN_TOKEN);
    }

    /**
     * 从 Token 中获取登录用户信息
     * <p>
     * 根据 Token 解析出登录用户标识，再从 Redis 中获取完整的用户信息。
     *
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *     <li>解析 Token 获取 loginUserKey</li>
     *     <li>拼接 Redis Key：login_user_key:{loginUserKey}</li>
     *     <li>从 Redis 获取 LoginUser 对象</li>
     * </ol>
     *
     * @param token JWT Token 字符串
     * @return LoginUser 对象，不存在返回 null
     */
    public LoginUser getLoginUser(String token) {
        // 从 Token 中获取登录用户标识
        String loginUserKey = getLoginUserKey(token);
        // 拼接完整 Redis Key 并查询
        return redisUtils.get(LOGIN_USER_KEY + ":" + loginUserKey, LoginUser.class);
    }

    /**
     * 删除 Token 对应的用户缓存
     * <p>
     * 退出登录时调用，删除 Redis 中缓存的用户信息。
     *
     * <p>
     * <b>使用场景：</b>
     * <ul>
     *     <li>用户主动退出登录</li>
     *     <li>用户被踢出（管理员操作）</li>
     *     <li>用户修改密码后强制下线</li>
     * </ul>
     *
     * @param token JWT Token 字符串
     * @return true 删除成功，false 删除失败（用户已下线）
     */
    public boolean removeToken(String token) {
        // 从 Token 获取登录用户标识
        String loginUserKey = getLoginUserKey(token);
        // 拼接 Redis Key 并删除
        // 返回是否删除成功
        return redisUtils.delete(LOGIN_USER_KEY + ":" + loginUserKey);
    }
}