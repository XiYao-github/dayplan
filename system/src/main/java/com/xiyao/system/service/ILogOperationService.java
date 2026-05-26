package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.LogOperation;
import com.xiyao.system.vo.LogOperationVo;

/**
 * 操作日志服务接口
 *
 * @author xiyao
 */
public interface ILogOperationService extends IService<LogOperation> {

    /**
     * 分页查询操作日志
     *
     * @param page      分页参数
     * @param username  用户账号（可选）
     * @param module    操作模块（可选）
     * @param logType   日志类型（可选，0=操作日志 1=审计日志）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param isAudit   是否审计管理员
     * @return 分页结果
     */
    Page<LogOperationVo> pageLogOperation(Page<LogOperationVo> page, String username, String module, Integer logType,
                                          Integer status, String startTime, String endTime, boolean isAudit);
}
