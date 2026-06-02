package com.xiyao.system.mapper;


import com.xiyao.common.base.mapper.MyBaseMapper;
import com.xiyao.system.entity.SysRegions;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行政区划 Mapper 接口
 * <p>
 * 继承 MyBatis-Plus BaseMapper，提供通用的 CRUD 操作能力。
 * 行政区划表使用区划代码（code）作为主键。
 *
 * @author xiyao
 * @see SysRegions
 */
@Mapper
public interface SysRegionsMapper extends MyBaseMapper<SysRegions> {

}