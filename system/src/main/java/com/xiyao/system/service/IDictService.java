package com.xiyao.system.service;

import java.util.List;
import java.util.Map;

/**
 * 字典服务接口
 * <p>
 * 提供字典数据的查询接口，供 DictCache 缓存使用。
 *
 * @author xiyao
 */
public interface IDictService {

    /**
     * 根据字典编码获取字典Map（dictValue -> dictLabel）
     */
    Map<String, String> getDictMap(String dictCode);

    /**
     * 获取所有字典数据
     */
    List<com.xiyao.system.entity.DictData> getAllDictData();
}
