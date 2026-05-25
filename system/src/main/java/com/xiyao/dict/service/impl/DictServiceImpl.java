package com.xiyao.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.dict.config.DictCache;
import com.xiyao.dict.service.DictService;
import com.xiyao.system.entity.DictData;
import com.xiyao.system.mapper.DictDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典服务实现类
 * <p>
 * 提供字典数据的查询和管理接口实现。
 * 实际字典缓存由 DictCache 管理，本服务负责与数据库交互。
 *
 * <p>
 * <b>主要职责：</b>
 * <ul>
 *     <li>通过 DictDataMapper 查询数据库中的字典数据</li>
 *     <li>构建字典值到标签的映射 Map</li>
 *     <li>提供缓存刷新功能，调用 DictCache 重新加载缓存</li>
 * </ul>
 *
 * @author xiyao
 * @see DictService
 * @see DictCache
 * @see DictDataMapper
 */
@Service
public class DictServiceImpl implements DictService {

    /**
     * 字典数据 Mapper
     * <p>
     * 用于查询 dict_data 表的字典数据
     */
    @Autowired
    private DictDataMapper dictDataMapper;

    /**
     * 根据字典编码获取字典Map（dictValue -> dictLabel）
     * <p>
     * 查询指定字典编码下的所有字典项，构建值到标签的映射。
     * 只查询状态为正常的字典项（status=1）。
     *
     * @param dictCode 字典编码
     * @return 字典值到标签的映射
     */
    @Override
    public Map<String, String> getDictMap(String dictCode) {
        // 查询该字典编码下的所有正常状态的字典
        List<DictData> list = getDictDataByType(dictCode);
        // 转换为 Map，key=字典值，value=字典标签
        // 使用 (v1, v2) -> v1 处理重复 key 的情况，保留第一个
        return list.stream().collect(Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (v1, v2) -> v1));
    }

    /**
     * 获取所有字典数据
     * <p>
     * 查询数据库中所有字典数据，不做状态过滤。
     *
     * @return 所有字典数据列表
     */
    @Override
    public List<DictData> getAllDictData() {
        return dictDataMapper.selectList(null);
    }

    /**
     * 根据字典类型获取字典数据列表
     * <p>
     * 只查询状态为正常的字典项（status=1），
     * 结果按 sort 字段排序。
     *
     * @param dictType 字典类型编码
     * @return 该类型的字典数据列表
     */
    @Override
    public List<DictData> getDictDataByType(String dictType) {
        // 构建查询条件：字典类型=指定值 且 状态=正常
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType)
                .eq(DictData::getStatus, 1); // 只查询正常状态的字典
        return dictDataMapper.selectList(wrapper);
    }

    /**
     * 刷新字典缓存
     * <p>
     * 调用 DictCache.refreshAll() 清除并重新加载所有字典和枚举缓存。
     * 用于管理员在后台修改字典数据后手动刷新缓存。
     */
    @Override
    public void refreshCache() {
        DictCache.getInstance().refreshAll();
    }
}