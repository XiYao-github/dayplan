package com.xiyao.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务（核心：JWT 生成、解析 + Redis 管理）
 *
 * 设计思路：
 *   - 使用 UUID 作为 Redis 的 key，真正的 JWT 字符串作为 value 的一部分（实际 JWT payload 中只存 UUID）
 *   - 每次请求携带 JWT，服务器解析出 UUID，再从 Redis 取完整的 LoginUser
 *   - 支持自动续期：如果剩余时间小于阈值，则自动延长 Redis 有效期
 */
@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建新 Token（登录成功后调用）
     *
     * 步骤：
     *   1. 生成随机 UUID 作为 Redis Key
     *   2. 设置 LoginUser 的 token、登录时间、过期时间
     *   3. 将 LoginUser 存入 Redis
     *   4. 生成 JWT 字符串（载荷中只存 UUID）
     *
     * @param loginUser 用户身份对象
     * @return JWT 字符串
     */
    public String createToken(LoginUser loginUser) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        loginUser.setToken(uuid);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + JwtConstants.EXPIRATION * 1000);

        // 存入 Redis
        String redisKey = JwtConstants.LOGIN_TOKEN_KEY + uuid;
        redisTemplate.opsForValue().set(redisKey, loginUser, JwtConstants.EXPIRATION, TimeUnit.SECONDS);

        // 生成 JWT（载荷中只包含 uuid）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.LOGIN_USER_KEY, uuid);
        return generateToken(claims);
    }

    /**
     * 从请求中获取登录用户（过滤器调用）
     *
     * @param request HTTP 请求
     * @return LoginUser 对象，若无效则返回 null
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 1. 从请求头获取 JWT
        String token = getTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        // 2. 解析 JWT，获取 UUID
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        String uuid = claims.get(JwtConstants.LOGIN_USER_KEY, String.class);
        // 3. 从 Redis 获取 LoginUser
        String redisKey = JwtConstants.LOGIN_TOKEN_KEY + uuid;
        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(redisKey);
        if (loginUser == null) {
            return null;
        }
        // 4. 自动续期（如果需要）
        verifyAndRefreshToken(loginUser);
        return loginUser;
    }

    /**
     * 验证并自动刷新 Token 有效期
     * 如果剩余有效期小于阈值，则重新设置过期时间
     */
    private void verifyAndRefreshToken(LoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        long expireTime = loginUser.getExpireTime();
        if (expireTime - currentTime <= JwtConstants.MILLIS_MINUTE_TEN) {
            // 续期
            long newExpireTime = currentTime + JwtConstants.EXPIRATION * 1000;
            loginUser.setExpireTime(newExpireTime);
            String redisKey = JwtConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
            redisTemplate.opsForValue().set(redisKey, loginUser, JwtConstants.EXPIRATION, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除 Token（登出时调用）
     */
    public void deleteToken(String token) {
        if (token == null) return;
        Claims claims = parseToken(token);
        if (claims != null) {
            String uuid = claims.get(JwtConstants.LOGIN_USER_KEY, String.class);
            String redisKey = JwtConstants.LOGIN_TOKEN_KEY + uuid;
            redisTemplate.delete(redisKey);
        }
    }

    // ---------- 内部辅助方法 ----------
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(JwtConstants.AUTH_HEADER);
        if (header != null && header.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return header.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, JwtConstants.SECRET)
                .setExpiration(new java.util.Date(System.currentTimeMillis() + JwtConstants.EXPIRATION * 1000))
                .compact();
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(JwtConstants.SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}