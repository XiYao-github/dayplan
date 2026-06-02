package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.SysAddress;

import java.util.List;

/**
 * 地址信息服务接口
 * <p>
 * 定义地址信息的业务操作方法，包括 CRUD、按父级查询、按级别查询。
 * 地址信息采用物理删除方式。
 *
 * @author xiyao
 * @see SysAddress
 */
public interface ISysAddressService extends IService<SysAddress> {

    /**
     * 查询地址列表
     *
     * @param query 查询条件，包含 name（可选）、level（可选）、parentCode（可选）
     * @return 符合条件的地址信息列表
     */
    List<SysAddress> list(SysAddress query);

    /**
     * 根据地址代码获取地址详情
     *
     * @param code 地址代码（主键）
     * @return 地址实体对象，若不存在则返回 null
     */
    SysAddress getById(Long code);

    /**
     * 创建地址信息
     *
     * @param address 地址实体对象
     * @return 保存是否成功
     */
    boolean create(SysAddress address);

    /**
     * 更新地址信息
     *
     * @param address 地址实体对象
     * @return 更新是否成功
     */
    boolean update(SysAddress address);

    /**
     * 删除地址信息（物理删除）
     *
     * @param code 地址代码（主键）
     * @return 删除是否成功
     */
    boolean delete(Long code);

    /**
     * 根据父级代码获取子级地址列表
     *
     * @param parentCode 父级地址代码
     * @return 直属子级地址列表
     */
    List<SysAddress> listByParentCode(Long parentCode);

    /**
     * 根据级别获取地址列表
     *
     * @param level 地址级别（1-省/直辖市、2-市、3-区/县）
     * @return 指定级别的地址列表
     */
    List<SysAddress> listByLevel(Integer level);
}