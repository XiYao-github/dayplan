package com.xiyao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.data.Result;
import com.xiyao.security.utils.SecurityUtils;
import com.xiyao.system.vo.LogLoginVo;
import com.xiyao.system.service.ILogLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证日志控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/log-login")
public class LogLoginController extends MyBaseController {

    private final ILogLoginService logLoginService;

    /**
     * 分页查询认证日志
     */
    @GetMapping("/list")
    public Result<Page<LogLoginVo>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer authType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        // 判断是否为审计管理员
        boolean isAudit = SecurityUtils.isAuditAdmin();
        // 非审计管理员只能查看自己的日志，强制 username 为当前用户
        if (!isAudit) {
            username = SecurityUtils.getUsername();
        }

        Page<LogLoginVo> page = new Page<>(pageNum, pageSize);
        Page<LogLoginVo> result = logLoginService.pageLogLogin(page, username, authType, status, startTime, endTime, isAudit);
        return Result.ok(result);
    }
}
