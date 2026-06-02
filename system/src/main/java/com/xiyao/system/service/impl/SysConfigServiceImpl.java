package com.xiyao.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.SysConfig;
import com.xiyao.system.mapper.SysConfigMapper;
import com.xiyao.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置服务实现类
 * <p>
 * 提供系统配置项的 CRUD 操作能力，支持按名称模糊搜索、按状态筛选。
 * 配置数据采用逻辑删除机制，删除操作实际为更新 deleted 字段。
 *
 * @author xiyao
 * @see ISysConfigService
 */
@Slf4j
@Service
public class SysConfigServiceImpl extends MyBaseServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    /**
     * 查询配置列表
     * <p>
     * 支持按配置名称模糊匹配、按状态精确筛选，自动过滤已删除数据。
     *
     * @param query 查询条件对象，包含 name（可选）、status（可选）
     * @return 符合条件的配置列表，从未删除的数据中查询
     */
    @Override
    public List<SysConfig> list(SysConfig query) {
        return Db.lambdaQuery(SysConfig.class)
                .like(ObjectUtil.isNotNull(query.getName()), SysConfig::getName, query.getName())
                .eq(ObjectUtil.isNotNull(query.getStatus()), SysConfig::getStatus, query.getStatus())
                .eq(SysConfig::getDeleted, 0)
                .list();
    }

    /**
     * 根据主键ID获取配置详情
     *
     * @param id 配置主键ID
     * @return 配置实体对象，若不存在或已删除则返回 null
     */
    @Override
    public SysConfig getById(Long id) {
        return Db.lambdaQuery(SysConfig.class)
                .eq(SysConfig::getId, id)
                .eq(SysConfig::getDeleted, 0)
                .one();
    }

    /**
     * 创建新配置
     * <p>
     * 初始化 deleted 字段为 0（未删除状态），然后保存到数据库。
     *
     * @param config 配置实体对象，包含 name、value、status 等信息
     * @return 保存是否成功
     */
    @Override
    public boolean create(SysConfig config) {
        config.setDeleted(0);
        return Db.save(config);
    }

    /**
     * 更新配置信息
     * <p>
     * 根据传入的配置ID查找现有记录，若存在则更新 name、value、status、remark 字段。
     * deleted 和 id 字段不会被修改。
     *
     * @param config 配置实体对象，包含要更新的字段值
     * @return 更新是否成功，若记录不存在则返回 false
     */
    @Override
    public boolean update(SysConfig config) {
        SysConfig existing = getById(config.getId());
        if (existing == null) {
            return false;
        }
        existing.setName(config.getName());
        existing.setValue(config.getValue());
        existing.setStatus(config.getStatus());
        existing.setRemark(config.getRemark());
        return Db.updateById(existing);
    }

    /**
     * 删除配置（逻辑删除）
     * <p>
     * 执行逻辑删除而非物理删除，将 deleted 字段设置为 1。
     * 此操作不可逆，删除后数据将不再出现在常规查询结果中。
     *
     * @param id 配置主键ID
     * @return 删除是否成功
     */
    @Override
    public boolean delete(Long id) {
        return Db.lambdaUpdate(SysConfig.class)
                .eq(SysConfig::getId, id)
                .set(SysConfig::getDeleted, 1)
                .update();
    }
}