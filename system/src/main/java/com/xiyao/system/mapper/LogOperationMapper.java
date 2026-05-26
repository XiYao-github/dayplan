package com.xiyao.system.mapper;

import com.xiyao.common.base.mapper.MyBaseMapper;
import com.xiyao.system.entity.LogOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 操作记录 Mapper 接口
 *
 * @author xiyao
 * @since 2026-05-20
 */
@Mapper
public interface LogOperationMapper extends MyBaseMapper<LogOperation> {

}
