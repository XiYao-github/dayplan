package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.SysRegions;

import java.util.List;

/**
 * 行政区划服务接口
 * <p>
 * 定义行政区划的业务操作方法，包括 CRUD、按父级查询、按级别查询。
 * 行政区划采用物理删除方式，删除时会递归删除子级。
 *
 * @author xiyao
 * @see SysRegions
 */
public interface ISysRegionsService extends IService<SysRegions> {

    /**
     * 查询行政区划列表
     *
     * @param query 查询条件，包含 name（可选）、level（可选）、parentCode（可选）
     * @return 符合条件的行政区划列表
     */
    List<SysRegions> list(SysRegions query);

    /**
     * 根据区划代码获取行政区划详情
     *
     * @param code 区划代码（主键）
     * @return 行政区划实体对象，若不存在则返回 null
     */
    SysRegions getById(Long code);

    /**
     * 创建行政区划
     *
     * @param regions 行政区划实体对象
     * @return 保存是否成功
     */
    boolean create(SysRegions regions);

    /**
     * 更新行政区划信息
     *
     * @param regions 行政区划实体对象
     * @return 更新是否成功
     */
    boolean update(SysRegions regions);

    /**
     * 删除行政区划（递归删除子级）
     *
     * @param code 区划代码（主键）
     * @return 删除是否成功
     */
    boolean delete(Long code);

    /**
     * 根据父级代码获取子级行政区划列表
     *
     * @param parentCode 父级区划代码
     * @return 直属子级行政区划列表
     */
    List<SysRegions> listByParentCode(Long parentCode);

    /**
     * 根据级别获取行政区划列表
     *
     * @param level 行政区划级别（1-省/直辖市、2-市、3-区/县）
     * @return 指定级别的行政区划列表
     */
    List<SysRegions> listByLevel(Integer level);
}