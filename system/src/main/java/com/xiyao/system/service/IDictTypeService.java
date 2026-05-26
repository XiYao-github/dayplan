package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.DictType;
import com.xiyao.system.vo.DictTypeVo;

/**
 * 字典类型服务接口
 *
 * @author xiyao
 */
public interface IDictTypeService extends IService<DictType> {

    /**
     * 分页查询字典类型
     */
    Page<DictTypeVo> pageDictType(Page<DictTypeVo> page, String dictName, String dictType, Integer status);

    /**
     * 获取字典类型详情
     */
    DictTypeVo getDictTypeById(Long id);

    /**
     * 创建字典类型
     */
    void createDictType(DictType dictType);

    /**
     * 更新字典类型
     */
    void updateDictType(DictType dictType);

    /**
     * 删除字典类型
     */
    void deleteDictType(Long id);
}
