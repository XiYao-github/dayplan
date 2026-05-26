package com.xiyao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyao.system.entity.DictData;
import com.xiyao.system.vo.DictDataVo;

import java.util.List;

/**
 * 字典数据服务接口
 *
 * @author xiyao
 */
public interface IDictDataService extends IService<DictData> {

    /**
     * 分页查询字典数据
     */
    Page<DictDataVo> pageDictData(Page<DictDataVo> page, String dictType, String dictLabel, Integer status);

    /**
     * 获取字典数据详情
     */
    DictDataVo getDictDataById(Long id);

    /**
     * 根据字典类型查询字典数据列表
     */
    List<DictDataVo> listByDictType(String dictType);

    /**
     * 创建字典数据
     */
    void createDictData(DictData dictData);

    /**
     * 更新字典数据
     */
    void updateDictData(DictData dictData);

    /**
     * 删除字典数据
     */
    void deleteDictData(Long id);

    /**
     * 刷新字典缓存
     */
    void refreshCache();
}
