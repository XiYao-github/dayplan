package com.xiyao.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.DictType;
import com.xiyao.system.mapper.DictTypeMapper;
import com.xiyao.system.service.IDictTypeService;
import com.xiyao.system.vo.DictTypeVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典类型服务实现
 *
 * @author xiyao
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends MyBaseServiceImpl<DictTypeMapper, DictType> implements IDictTypeService {

    @Override
    public Page<DictTypeVo> pageDictType(Page<DictTypeVo> page, String dictName, String dictType, Integer status) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dictName), DictType::getDictName, dictName)
                .like(StringUtils.isNotBlank(dictType), DictType::getDictType, dictType)
                .eq(status != null, DictType::getStatus, status)
                .orderByDesc(DictType::getId);

        Page<DictType> resultPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return convertToVoPage(resultPage, DictTypeVo.class, this::convertToVo);
    }

    @Override
    public DictTypeVo getDictTypeById(Long id) {
        DictType entity = getById(id);
        return entity != null ? convertToVo(entity) : null;
    }

    @Override
    public void createDictType(DictType dictType) {
        save(dictType);
    }

    @Override
    public void updateDictType(DictType dictType) {
        updateById(dictType);
    }

    @Override
    public void deleteDictType(Long id) {
        removeById(id);
    }

    private DictTypeVo convertToVo(DictType entity) {
        DictTypeVo vo = new DictTypeVo();
        vo.setId(entity.getId());
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(entity.getStatus() != null && entity.getStatus() == 1 ? "正常" : "停用");
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
