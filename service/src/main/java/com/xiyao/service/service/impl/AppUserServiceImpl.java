package com.xiyao.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.service.entity.AppUser;
import com.xiyao.service.mapper.AppUserMapper;
import com.xiyao.service.service.AppUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 小程序用户 服务实现类
 * </p>
 *
 * @author xiyao
 */
@Service
public class AppUserServiceImpl extends MyBaseServiceImpl<AppUserMapper, AppUser> implements AppUserService {

    @Override
    public AppUser getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getOpenid, openid)
                .eq(AppUser::getStatus, 1));
    }

    @Override
    public AppUser login(String openid, String ip) {
        AppUser user = getByOpenid(openid);

        if (ObjectUtil.isNull(user)) {
            // 新用户注册
            user = new AppUser()
                    .setOpenid(openid)
                    .setStatus(1)
                    .setLastLoginIp(ip)
                    .setLastLoginTime(LocalDateTime.now());
            save(user);
        } else {
            // 更新登录信息
            user.setLastLoginIp(ip)
                    .setLastLoginTime(LocalDateTime.now());
            updateById(user);
        }

        return user;
    }
}
