package com.xiyao.common.config;

import com.xiyao.system.entity.SysDictData;
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
    Map<String, Set<SysDictData>> dictMap = new ConcurrentHashMap<>();



}
