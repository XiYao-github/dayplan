package com.xiyao.framework.security;

/**
 * JWT 常量配置
 * 作用：集中管理 JWT 和 Redis 相关的常量，避免魔法值
 */
public class JwtConstants {

    /** 请求头中存放 token 的 key */
    public static final String AUTH_HEADER = "Authorization";

    /** token 前缀（Bearer ） */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Redis 中存储登录用户信息的 key 前缀 */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /** JWT 载荷中存放用户唯一标识（uuid）的字段名 */
    public static final String LOGIN_USER_KEY = "user_key";

    /** 令牌有效期（单位：秒），可从配置文件读取，这里默认 7200 秒 = 2 小时 */
    public static final Long EXPIRATION = 7200L;

    /** 令牌密钥（实际应放在配置文件中，这里仅为示例） */
    public static final String SECRET = "your-secret-key-change-it";

    /** 自动刷新阈值（毫秒），剩余时间小于 20 分钟时刷新 */
    public static final long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    /** 超级管理员权限标识（用于绕过权限校验） */
    public static final String ALL_PERMISSION = "*:*:*";
}
