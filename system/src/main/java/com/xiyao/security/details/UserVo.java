package com.xiyao.security.details;

import lombok.Data;

/**
 * 登录用户信息
 */
@Data
public class UserVo {

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;
}
