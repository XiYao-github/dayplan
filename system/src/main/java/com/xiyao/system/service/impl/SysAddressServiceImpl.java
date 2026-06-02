package com.xiyao.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.SysAddress;
import com.xiyao.system.mapper.SysAddressMapper;
import com.xiyao.system.service.ISysAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地址信息服务实现类
 * <p>
 * 提供地址信息的 CRUD 操作，支持按父级代码查询子级、按级别查询等功能。
 * 地址信息采用物理删除方式。
 *
 * @author xiyao
 * @see ISysAddressService
 */
@Slf4j
@Service
public class SysAddressServiceImpl extends MyBaseServiceImpl<SysAddressMapper, SysAddress> implements ISysAddressService {

    /**
     * 查询地址列表
     * <p>
     * 支持按名称模糊匹配、按级别精确筛选、按父级代码筛选，
     * 结果按排序字段升序排列。
     *
     * @param query 查询条件对象，包含 name（可选）、level（可选）、parentCode（可选）
     * @return 符合条件的地址信息列表
     */
    @Override
    public List<SysAddress> list(SysAddress query) {
        return Db.lambdaQuery(SysAddress.class)
                .like(ObjectUtil.isNotNull(query.getName()), SysAddress::getName, query.getName())
                .eq(ObjectUtil.isNotNull(query.getLevel()), SysAddress::getLevel, query.getLevel())
                .eq(ObjectUtil.isNotNull(query.getParentCode()), SysAddress::getParentCode, query.getParentCode())
                .orderByAsc(SysAddress::getSort)
                .list();
    }

    /**
     * 根据地址代码获取地址详情
     *
     * @param code 地址代码（主键）
     * @return 地址实体对象，若不存在则返回 null
     */
    @Override
    public SysAddress getById(Long code) {
        return Db.lambdaQuery(SysAddress.class)
                .eq(SysAddress::getCode, code)
                .one();
    }

    /**
     * 创建地址信息
     * <p>
     * 将新的地址数据保存到数据库。
     *
     * @param address 地址实体对象
     * @return 保存是否成功
     */
    @Override
    public boolean create(SysAddress address) {
        return Db.save(address);
    }

    /**
     * 更新地址信息
     * <p>
     * 根据传入的地址代码查找现有记录，若存在则更新 name、parentCode、sort 字段。
     * 地址代码（code）作为主键不可修改。
     *
     * @param address 地址实体对象，包含要更新的字段值
     * @return 更新是否成功，若记录不存在则返回 false
     */
    @Override
    public boolean update(SysAddress address) {
        SysAddress existing = getById(address.getCode());
        if (existing == null) {
            return false;
        }
        existing.setName(address.getName());
        existing.setParentCode(address.getParentCode());
        existing.setSort(address.getSort());
        return Db.updateById(existing);
    }

    /**
     * 删除地址信息（物理删除）
     *
     * @param code 地址代码（主键）
     * @return 删除是否成功
     */
    @Override
    public boolean delete(Long code) {
        return Db.lambdaUpdate(SysAddress.class)
                .eq(SysAddress::getCode, code)
                .remove();
    }

    /**
     * 根据父级代码获取子级地址列表
     *
     * @param parentCode 父级地址代码
     * @return 直属子级地址列表，按排序字段升序排列
     */
    @Override
    public List<SysAddress> listByParentCode(Long parentCode) {
        return Db.lambdaQuery(SysAddress.class)
                .eq(SysAddress::getParentCode, parentCode)
                .orderByAsc(SysAddress::getSort)
                .list();
    }

    /**
     * 根据级别获取地址列表
     * <p>
     * 级别说明：1-省/直辖市、2-市、3-区/县/县级市
     *
     * @param level 地址级别（1/2/3）
     * @return 指定级别的地址列表，按排序字段升序排列
     */
    @Override
    public List<SysAddress> listByLevel(Integer level) {
        return Db.lambdaQuery(SysAddress.class)
                .eq(SysAddress::getLevel, level)
                .orderByAsc(SysAddress::getSort)
                .list();
    }
}