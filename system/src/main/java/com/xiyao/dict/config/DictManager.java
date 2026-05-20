package com.xiyao.dict.config;

import com.xiyao.system.entity.DictData;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典管理类
 */
@Slf4j
@NoArgsConstructor
public class DictManager {

    /**
     * 数据字典缓存
     */
    Map<String, Set<DictData>> dictMap = new ConcurrentHashMap<>();



}
