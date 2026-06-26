package com.xiyao.service.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.service.service.AppUserService;
import com.xiyao.service.vo.AppLoginVo;
import com.xiyao.service.vo.AppUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 小程序用户 前端控制器
 * </p>
 *
 * @author xiyao
 */
@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
public class AppUserController extends MyBaseController {

    private final AppUserService appUserService;

    /**
     * 小程序登录
     *
     * @param code 微信code
     * @param ip 登录IP
     * @return 登录结果
     */
    @PostMapping("/login")
    public AppLoginVo login(@RequestParam String code, @RequestParam(required = false) String ip) {
        // TODO: 调用微信接口换取openid，这里先模拟
        String openid = "mock_openid_" + code;
        var user = appUserService.login(openid, ip);

        return new AppLoginVo()
                .setUserId(user.getId())
                .setToken("mock_token_" + user.getId());
    }

    /**
     * 获取个人信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public AppUserVo getInfo(@RequestParam Long userId) {
        var user = appUserService.getById(userId);

        return new AppUserVo()
                .setId(user.getId())
                .setNickname(user.getNickname())
                .setPhone(user.getPhone())
                .setAvatar(user.getAvatar());
    }

    /**
     * 更新个人信息
     *
     * @param userId 用户ID
     * @param vo 更新信息
     * @return 更新结果
     */
    @PutMapping("/info")
    public void updateInfo(@RequestParam Long userId, @RequestBody AppUserVo vo) {
        var user = appUserService.getById(userId);
        if (user == null) {
            error("用户不存在");
            return;
        }

        user.setNickname(vo.getNickname())
                .setPhone(vo.getPhone())
                .setAvatar(vo.getAvatar());
        appUserService.updateById(user);
    }
}
