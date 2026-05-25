package com.xiyao.dict.service;

import com.xiyao.system.entity.DictData;

import java.util.List;
import java.util.Map;

/**
 * 字典服务接口
 * <p>
 * 提供字典数据的查询和管理接口。
 * 实际字典缓存由 DictCache 管理，本服务负责与数据库交互。
 *
 * <p>
 * <b>主要功能：</b>
 * <ul>
 *     <li>根据字典编码查询字典Map</li>
 *     <li>获取所有字典数据</li>
 *     <li>根据字典类型查询字典数据列表</li>
 *     <li>刷新字典缓存</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.dict.service.impl.DictServiceImpl
 * @see com.xiyao.dict.config.DictCache
 */
public interface DictService {

    /**
     * 根据字典编码获取字典Map（dictValue -> dictLabel）
     * <p>
     * 查询指定字典编码下的所有字典项，构建值到标签的映射。
     * 只查询状态为正常的字典项。
     *
     * @param dictCode 字典编码
     * @return 字典值到标签的映射
     */
    Map<String, String> getDictMap(String dictCode);

    /**
     * 获取所有字典数据
     * <p>
     * 查询数据库中所有字典数据，包括正常和已删除的数据。
     *
     * @return 所有字典数据列表
     */
    List<DictData> getAllDictData();

    /**
     * 根据字典类型获取字典数据列表
     * <p>
     * 只查询状态为正常的字典项，结果按 sort 字段排序。
     *
     * @param dictType 字典类型编码
     * @return 该类型的字典数据列表
     */
    List<DictData> getDictDataByType(String dictType);

    /**
     * 刷新字典缓存
     * <p>
     * 调用 DictCache.refreshAll() 清除并重新加载所有字典和枚举缓存。
     * 用于管理员在后台修改字典数据后手动刷新缓存。
     */
    void refreshCache();
}