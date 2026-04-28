package com.xiyao.app;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.AppUser;
import com.xiyao.system.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class AdminApplicationTests {


    @Autowired
    private AppUserMapper appUserMapper;

    @Test
    public void testInsert() {
        AppUser user = new AppUser();
        user.setOpenid("wx_openid_123456");
        user.setName("张三");
        user.setPhone("13812345678");
        user.setSex((byte) 1);  // 1-男
        user.setAvatar("https://example.com/avatar.jpg");
        user.setRegions("广东省深圳市");
        user.setLoginIp("192.168.1.100");
        user.setLoginDate(LocalDateTime.now());
        user.setStatus((byte) 1);  // 1-正常
        user.setRemark("测试用户");

        int rows = appUserMapper.insert(user);
        System.out.println("插入结果: " + rows);

        AppUser appUser = appUserMapper.selectById(user.getId());
        System.out.println(appUser);
    }


    @Test
    public void testWrapperUpdate() {
        // 之前拦截不到的写法，现在能加密了 ✅
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getId, 1L)
                .set(AppUser::getName, "新名字")   // 现在能加密了
                .set(AppUser::getPhone, "13900001111"); // 现在能加密了

        appUserMapper.update(null, wrapper);

        // 验证：数据库里 name 和 phone 应该是密文 ✅
        AppUser user = appUserMapper.selectById(1L);
        System.out.println("姓名: " + user.getName());   // 应该是 "新名字"
        System.out.println("手机: " + user.getPhone());  // 应该是 "13900001111"
    }

}
