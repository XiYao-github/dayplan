package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小程序用户 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class AppUserVo {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 电话
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;
}
