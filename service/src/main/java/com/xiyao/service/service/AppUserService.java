package com.xiyao.service.service;

import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.service.entity.AppUser;

/**
 * <p>
 * 小程序用户 服务类
 * </p>
 *
 * @author xiyao
 */
public interface AppUserService extends MyBaseService<AppUser> {

    /**
     * 根据openid获取用户
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    AppUser getByOpenid(String openid);

    /**
     * 小程序登录
     *
     * @param openid 微信openid
     * @param ip 登录IP
     * @return 用户信息
     */
    AppUser login(String openid, String ip);
}
