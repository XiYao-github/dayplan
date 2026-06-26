package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小程序登录响应 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class AppLoginVo {

    /**
     * JWT token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;
}
