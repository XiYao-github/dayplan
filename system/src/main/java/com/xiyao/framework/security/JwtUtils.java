package com.xiyao.framework.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {

    // 密钥
    private static final byte[] SECRET = "secret".getBytes(StandardCharsets.UTF_8);

    // 过期时间（单位：毫秒）
    private static final long EXPIRATION = 60 * 60 * 1000L; // 1小时

    /**
     * 生成Token
     */
    public static String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return JWT.create()
                .setPayload("username", username)   // 设置自定义载荷
                .setIssuedAt(now)      // 设置签发时间
                .setExpiresAt(expiration) // 设置过期时间
                .setKey(SECRET) // 设置密钥
                .sign();               // 生成签名
    }

    /**
     * 验证Token（仅验证签名和有效期）
     */
    public static boolean validateToken(String token) {
        try {
            // 验证签名
            if (!JWTUtil.verify(token, SECRET)) {
                return false;
            }
            // 验证有效期
            JWTValidator.of(token).validateDate(DateUtil.date());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 username
     */
    public static String getUsername(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return jwt.getPayload("username").toString();
    }
}