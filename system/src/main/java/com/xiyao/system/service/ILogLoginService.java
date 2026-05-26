package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.LogLogin;
import com.xiyao.system.vo.LogLoginVo;

/**
 * 认证日志服务接口
 *
 * @author xiyao
 */
public interface ILogLoginService extends IService<LogLogin> {

    /**
     * 分页查询认证日志
     *
     * @param page      分页参数
     * @param username  用户账号（可选）
     * @param authType  认证类型（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param isAudit   是否审计管理员
     * @return 分页结果
     */
    Page<LogLoginVo> pageLogLogin(Page<LogLoginVo> page, String username, Integer authType, Integer status,
                                  String startTime, String endTime, boolean isAudit);
}
