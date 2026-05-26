package com.xiyao.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.LogLogin;
import com.xiyao.system.vo.LogLoginVo;
import com.xiyao.system.mapper.LogLoginMapper;
import com.xiyao.system.service.ILogLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证日志服务实现
 *
 * @author xiyao
 */
@Service
@RequiredArgsConstructor
public class LogLoginServiceImpl extends MyBaseServiceImpl<LogLoginMapper, LogLogin> implements ILogLoginService {

    @Override
    public Page<LogLoginVo> pageLogLogin(Page<LogLoginVo> page, String username, Integer authType, Integer status,
                                          String startTime, String endTime, boolean isAudit) {
        LambdaQueryWrapper<LogLogin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(!isAudit, LogLogin::getUsername, username)
                .eq(authType != null, LogLogin::getAuthType, authType)
                .eq(status != null, LogLogin::getStatus, status)
                .ge(StringUtils.isNotBlank(startTime), LogLogin::getLoginTime, startTime)
                .le(StringUtils.isNotBlank(endTime), LogLogin::getLoginTime, endTime)
                .orderByDesc(LogLogin::getLoginTime);

        Page<LogLogin> resultPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return convertToVoPage(resultPage, LogLoginVo.class, this::convertToVo);
    }

    private LogLoginVo convertToVo(LogLogin entity) {
        LogLoginVo vo = new LogLoginVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setAuthType(entity.getAuthType());
        vo.setAuthTypeDesc(getAuthTypeDesc(entity.getAuthType()));
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(entity.getStatus() != null && entity.getStatus() == 1 ? "成功" : "失败");
        vo.setMessage(entity.getMessage());
        vo.setIp(entity.getIp());
        vo.setOs(entity.getOs());
        vo.setBrowser(entity.getBrowser());
        vo.setPlatform(entity.getPlatform());
        vo.setTraceId(entity.getTraceId());
        vo.setLoginTime(entity.getLoginTime());
        return vo;
    }

    private String getAuthTypeDesc(Integer authType) {
        if (authType == null) return "未知";
        return switch (authType) {
            case 7 -> "登录";
            case 8 -> "登出";
            case 9 -> "注册";
            default -> "未知";
        };
    }
}
