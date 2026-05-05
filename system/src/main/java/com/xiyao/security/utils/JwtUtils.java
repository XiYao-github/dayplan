package com.xiyao.security.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {


    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN = "login_token";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key:";

    // 密钥
    private static final byte[] SECRET = "secret".getBytes(StandardCharsets.UTF_8);

    // 过期时间（单位：秒）
    public static final long SECONDS = 60 * 60; // 1小时

    // 过期时间（单位：毫秒）
    public static final long MILLISECONDS = 60 * 60 * 1000; // 1小时

    /**
     * 生成Token
     */
    public String generateToken(String loginUserKey) {
        Map<String, Object> map = new HashMap<>();
        map.put(LOGIN_TOKEN, loginUserKey);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + MILLISECONDS);

        return JWT.create()
                .addPayloads(map)           // 设置载荷信息
                .setIssuedAt(now)           // 设置签发时间
                .setExpiresAt(expiration)   // 设置过期时间
                .setKey(SECRET)             // 设置密钥
                .sign();                    // 生成签名
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        JWT jwt = JWT.of(token).setKey(SECRET);
        // 验证签名，验证有效期
        return jwt.verify() && jwt.validate(0);
    }

    /**
     * 获取登录token
     */
    public String getLoginTokenKey(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return (String) jwt.getPayload(LOGIN_TOKEN);
    }
}