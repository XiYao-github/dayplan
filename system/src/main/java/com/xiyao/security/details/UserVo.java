package com.xiyao.security.details;

import lombok.Data;

/**
 * 登录用户请求对象
 * <p>
 * 用于接收前端传来的登录/注册请求参数，
 * 包含用户名、密码和验证码等登录凭证信息。
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>用户登录时提交用户名和密码</li>
 *     <li>用户注册时提交用户名和密码</li>
 *     <li>验证码校验场景</li>
 * </ul>
 *
 * <p>
 * <b>与其他类的关系：</b>
 * <ul>
 *     <li>与 LoginUser 的区别：UserVo 是请求参数，LoginUser 是登录后的用户信息</li>
 *     <li>与 SysUser 的区别：UserVo 是简化版请求对象，SysUser 是完整数据库实体</li>
 * </ul>
 *
 * @author xiyao
 * @see LoginUser
 */
@Data
public class UserVo {

    /**
     * 用户账号
     * <p>
     * 用户的唯一标识，用于登录认证。
     */
    private String username;

    /**
     * 密码
     * <p>
     * 用户密码，传输时建议使用 HTTPS 加密。
     * 注册时会使用 BCrypt 算法加密存储。
     */
    private String password;

    /**
     * 验证码
     * <p>
     * 用于图形验证码或短信验证码校验。
     * 可选字段，根据业务需求使用。
     */
    private String code;
}