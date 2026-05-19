package com.xiyao.security.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.xiyao.common.utils.RedisUtils;
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
 */
@Slf4j
@AllArgsConstructor
public class JwtUtils {

    private final RedisUtils redisUtils;

    private final SecurityData properties;

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN = "login_token";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 令牌前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 获取密钥
     */
    private byte[] getSecret() {
        return properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 过期时间（单位：秒）
     */
    private Long getSeconds() {
        return properties.getJwt().getExpire();
    }

    /**
     * 过期时间（单位：毫秒）
     */
    private Long getMilliseconds() {
        return properties.getJwt().getExpire() * 1000;
    }

    /**
     * 获取 token
     */
    public String getToken(LoginUser loginUser) {
        // 生成登录用户 key
        String loginUserKey = IdUtil.fastSimpleUUID();
        // 缓存登录用户
        redisUtils.set(LOGIN_USER_KEY + ":" + loginUserKey, loginUser, getSeconds());
        return generateToken(loginUserKey);
    }

    /**
     * 生成 token
     */
    public String generateToken(String loginUserKey) {
        Map<String, Object> map = new HashMap<>();
        map.put(LOGIN_TOKEN, loginUserKey);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + getMilliseconds());

        return JWT.create()
                .addPayloads(map)           // 设置载荷信息
                .setIssuedAt(now)           // 设置签发时间
                .setExpiresAt(expiration)   // 设置过期时间
                .setKey(getSecret())        // 设置密钥
                .sign();                    // 生成签名
    }

    /**
     * 获取请求头 Token
     */
    public String getHeaderToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.replace(TOKEN_PREFIX, "");
        }
        return bearerToken;
    }

    /**
     * 验证 token
     */
    public boolean validateToken(String token) {
        try {
            JWT jwt = JWT.of(token).setKey(getSecret());
            // 验证签名，验证有效期
            return jwt.verify() && jwt.validate(0);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取登录用户 key
     */
    public String getLoginUserKey(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return (String) jwt.getPayload(LOGIN_TOKEN);
    }

    /**
     * 获取登录用户
     */
    public LoginUser getLoginUser(String token) {
        String loginUserKey = getLoginUserKey(token);
        return redisUtils.get(LOGIN_USER_KEY + ":" + loginUserKey, LoginUser.class);
    }

    /**
     * 移除 token 缓存的用户信息
     */
    public boolean removeToken(String token) {
        String loginUserKey = getLoginUserKey(token);
        return redisUtils.delete(LOGIN_USER_KEY + ":" + loginUserKey);
    }

}