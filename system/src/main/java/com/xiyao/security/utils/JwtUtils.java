package com.xiyao.security.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.xiyao.common.utils.RedisUtils;
import com.xiyao.security.details.LoginUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

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

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN = "login_token";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 密钥
     */
    private static byte[] secret;

    /**
     * 过期时间（单位：秒） 1小时
     */
    public static long seconds;

    /**
     * 过期时间（单位：毫秒） 1小时
     */
    public static long milliseconds;

    @Value("${jwt.secret}")
    public static void setSecret(String secret) {
        JwtUtils.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Value("${jwt.expire}")
    public static void setSeconds(Long expire) {
        JwtUtils.seconds = expire;
    }

    @Value("${jwt.expire}")
    public static void setMilliseconds(Long expire) {
        JwtUtils.milliseconds = expire * 1000;
    }

    /**
     * 生成 token
     */
    public String generateToken(String loginUserKey) {
        Map<String, Object> map = new HashMap<>();
        map.put(LOGIN_TOKEN, loginUserKey);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + milliseconds);

        return JWT.create()
                .addPayloads(map)           // 设置载荷信息
                .setIssuedAt(now)           // 设置签发时间
                .setExpiresAt(expiration)   // 设置过期时间
                .setKey(secret)             // 设置密钥
                .sign();                    // 生成签名
    }

    /**
     * 验证 token
     */
    public boolean validateToken(String token) {
        try {
            JWT jwt = JWT.of(token).setKey(secret);
            // 验证签名，验证有效期
            return jwt.verify() && jwt.validate(0);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 token
     */
    public String getToken(LoginUser loginUser) {
        // 生成登录用户 key
        String loginUserKey = IdUtil.fastSimpleUUID();
        // 缓存登录用户
        redisUtils.set(LOGIN_USER_KEY + ":" + loginUserKey, loginUser, JwtUtils.seconds);
        return generateToken(loginUserKey);
    }

    /**
     * 获取登录 token
     */
    public String getLoginTokenKey(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return (String) jwt.getPayload(LOGIN_TOKEN);
    }

    /**
     * 获取登录用户
     */
    public LoginUser getLoginUser(String token) {
        String loginTokenKey = getLoginTokenKey(token);
        return redisUtils.get(LOGIN_USER_KEY + ":" + loginTokenKey, LoginUser.class);
    }

    /**
     * 删除 token 缓存的用户信息
     *
     * @param token 需要删除的 token
     */
    public boolean deleteToken(String token) {
        String loginTokenKey = getLoginTokenKey(token);
        return redisUtils.delete(LOGIN_USER_KEY + ":" + loginTokenKey);
    }

}