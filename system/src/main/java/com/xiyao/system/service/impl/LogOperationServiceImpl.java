package com.xiyao.system.service.impl;

import com.xiyao.system.entity.LogOperation;
import com.xiyao.system.mapper.LogOperationMapper;
import com.xiyao.system.service.LogOperationService;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作记录 服务实现类
 * </p>
 *
 * @author xiyao
 * @since 2026-06-09
 */
@Service
public class LogOperationServiceImpl extends MyBaseServiceImpl<LogOperationMapper, LogOperation> implements LogOperationService {

}
