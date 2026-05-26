package com.xiyao.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.LogOperation;
import com.xiyao.system.vo.LogOperationVo;
import com.xiyao.system.mapper.LogOperationMapper;
import com.xiyao.system.service.ILogOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 *
 * @author xiyao
 */
@Service
@RequiredArgsConstructor
public class LogOperationServiceImpl extends MyBaseServiceImpl<LogOperationMapper, LogOperation> implements ILogOperationService {

    @Override
    public Page<LogOperationVo> pageLogOperation(Page<LogOperationVo> page, String username, String module,
                                                  Integer logType, Integer status, String startTime, String endTime,
                                                  boolean isAudit) {
        LambdaQueryWrapper<LogOperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(!isAudit, LogOperation::getUsername, username)
                .like(StringUtils.isNotBlank(module), LogOperation::getOperationModule, module)
                // 审计管理员可查看所有日志，普通管理员只看操作日志
                .eq(logType != null, LogOperation::getLogType, logType)
                .eq(!isAudit && logType == null, LogOperation::getLogType, 0)
                .eq(status != null, LogOperation::getStatus, status)
                .ge(StringUtils.isNotBlank(startTime), LogOperation::getOperationTime, startTime)
                .le(StringUtils.isNotBlank(endTime), LogOperation::getOperationTime, endTime)
                .orderByDesc(LogOperation::getOperationTime);

        Page<LogOperation> resultPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return convertToVoPage(resultPage, LogOperationVo.class, this::convertToVo);
    }

    private LogOperationVo convertToVo(LogOperation entity) {
        LogOperationVo vo = new LogOperationVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setAdminType(entity.getAdminType());
        vo.setAdminTypeDesc(getAdminTypeDesc(entity.getAdminType()));
        vo.setLogType(entity.getLogType());
        vo.setLogTypeDesc(entity.getLogType() != null && entity.getLogType() == 1 ? "审计日志" : "操作日志");
        vo.setOperationModule(entity.getOperationModule());
        vo.setOperationMethod(entity.getOperationMethod());
        vo.setOperationType(entity.getOperationType());
        vo.setOperationTypeDesc(getOperationTypeDesc(entity.getOperationType()));
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(entity.getStatus() != null && entity.getStatus() == 1 ? "成功" : "失败");
        vo.setMessage(entity.getMessage());
        vo.setRequestParam(entity.getRequestParam());
        vo.setReturnResult(entity.getReturnResult());
        vo.setCostTime(entity.getCostTime());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setRequestUrl(entity.getRequestUrl());
        vo.setIp(entity.getIp());
        vo.setLocation(entity.getLocation());
        vo.setOs(entity.getOs());
        vo.setBrowser(entity.getBrowser());
        vo.setPlatform(entity.getPlatform());
        vo.setTraceId(entity.getTraceId());
        vo.setOperationTime(entity.getOperationTime());
        return vo;
    }

    private String getAdminTypeDesc(Integer adminType) {
        if (adminType == null) return "普通用户";
        return switch (adminType) {
            case 1 -> "系统管理员";
            case 2 -> "安全管理员";
            case 3 -> "审计管理员";
            default -> "普通用户";
        };
    }

    private String getOperationTypeDesc(Integer operationType) {
        if (operationType == null) return "其它";
        return switch (operationType) {
            case 1 -> "查询";
            case 2 -> "新增";
            case 3 -> "更新";
            case 4 -> "删除";
            case 5 -> "导出";
            case 6 -> "导入";
            case 7 -> "登录";
            case 8 -> "登出";
            case 9 -> "注册";
            default -> "其它";
        };
    }
}
