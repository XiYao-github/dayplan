package com.xiyao.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.dict.utils.DictCache;
import com.xiyao.system.entity.DictData;
import com.xiyao.system.mapper.DictDataMapper;
import com.xiyao.system.service.IDictDataService;
import com.xiyao.system.vo.DictDataVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典数据服务实现
 *
 * @author xiyao
 */
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends MyBaseServiceImpl<DictDataMapper, DictData> implements IDictDataService {

    private final DictCache dictCache;

    @Override
    public Page<DictDataVo> pageDictData(Page<DictDataVo> page, String dictType, String dictLabel, Integer status) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dictType), DictData::getDictType, dictType)
                .like(StringUtils.isNotBlank(dictLabel), DictData::getDictLabel, dictLabel)
                .eq(status != null, DictData::getStatus, status)
                .orderByDesc(DictData::getId);

        Page<DictData> resultPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return convertToVoPage(resultPage, DictDataVo.class, this::convertToVo);
    }

    @Override
    public DictDataVo getDictDataById(Long id) {
        DictData entity = getById(id);
        return entity != null ? convertToVo(entity) : null;
    }

    @Override
    public List<DictDataVo> listByDictType(String dictType) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType)
                .eq(DictData::getStatus, 1)
                .orderByAsc(DictData::getId);
        List<DictData> list = list(wrapper);
        return list.stream().map(this::convertToVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictData(DictData dictData) {
        save(dictData);
        dictCache.refreshAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(DictData dictData) {
        updateById(dictData);
        dictCache.refreshAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        removeById(id);
        dictCache.refreshAll();
    }

    @Override
    public void refreshCache() {
        dictCache.refreshAll();
    }

    private DictDataVo convertToVo(DictData entity) {
        DictDataVo vo = new DictDataVo();
        vo.setId(entity.getId());
        vo.setDictType(entity.getDictType());
        vo.setDictCode(entity.getDictCode());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}