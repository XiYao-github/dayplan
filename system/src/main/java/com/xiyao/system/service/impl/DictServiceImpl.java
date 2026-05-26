package com.xiyao.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.system.entity.DictData;
import com.xiyao.system.mapper.DictDataMapper;
import com.xiyao.system.service.IDictService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典服务实现
 *
 * @author xiyao
 */
@Service
public class DictServiceImpl extends MyBaseServiceImpl<DictDataMapper, DictData> implements IDictService {

    @Override
    public Map<String, String> getDictMap(String dictCode) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictCode)
                .eq(DictData::getStatus, 1);
        List<DictData> list = list(wrapper);
        return list.stream().collect(Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (v1, v2) -> v1));
    }

    @Override
    public List<DictData> getAllDictData() {
        return list();
    }
}
