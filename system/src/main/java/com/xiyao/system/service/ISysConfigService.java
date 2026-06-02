package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 * <p>
 * 定义系统配置项的业务操作方法，包括 CRUD 和条件查询。
 * 配置数据采用逻辑删除机制。
 *
 * @author xiyao
 * @see SysConfig
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 查询配置列表
     *
     * @param query 查询条件，包含 name（可选）、status（可选）
     * @return 符合条件的配置列表
     */
    List<SysConfig> list(SysConfig query);

    /**
     * 根据主键ID获取配置详情
     *
     * @param id 配置主键ID
     * @return 配置实体对象，若不存在或已删除则返回 null
     */
    SysConfig getById(Long id);

    /**
     * 创建新配置
     *
     * @param config 配置实体对象
     * @return 保存是否成功
     */
    boolean create(SysConfig config);

    /**
     * 更新配置信息
     *
     * @param config 配置实体对象
     * @return 更新是否成功
     */
    boolean update(SysConfig config);

    /**
     * 删除配置（逻辑删除）
     *
     * @param id 配置主键ID
     * @return 删除是否成功
     */
    boolean delete(Long id);
}