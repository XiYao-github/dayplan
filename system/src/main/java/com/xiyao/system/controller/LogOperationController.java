package com.xiyao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.data.Result;
import com.xiyao.security.utils.SecurityUtils;
import com.xiyao.system.vo.LogOperationVo;
import com.xiyao.system.service.ILogOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/log-operation")
public class LogOperationController extends MyBaseController {

    private final ILogOperationService logOperationService;

    /**
     * 分页查询操作日志
     * <p>
     * 审计管理员可查看所有日志（含审计日志），普通管理员只能查看操作日志
     */
    @GetMapping("/list")
    public Result<Page<LogOperationVo>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer logType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        // 判断是否为审计管理员
        boolean isAudit = SecurityUtils.isAuditAdmin();
        // 非审计管理员只能查看自己的日志，强制 username 为当前用户
        if (!isAudit) {
            username = SecurityUtils.getUsername();
        }

        Page<LogOperationVo> page = new Page<>(pageNum, pageSize);
        Page<LogOperationVo> result = logOperationService.pageLogOperation(page, username, module, logType, status, startTime, endTime, isAudit);
        return Result.ok(result);
    }
}
