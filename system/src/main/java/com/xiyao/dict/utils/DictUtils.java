package com.xiyao.dict.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.DictData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 数据字典缓存管理器
 * <p>
 * 负责管理字典数据缓存，提供字典标签查询功能。
 * 使用静态方法实现，直接通过类名调用。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>字典缓存管理：存储 dictType -> (dictValue -> dictLabel) 的映射</li>
 *     <li>线程安全：使用读写锁保证并发安全</li>
 *     <li>全量加载：应用启动时加载所有字典数据到缓存</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 获取字典标签
 * String label = DictUtils.getDictLabel("status", "1");
 *
 * // 刷新字典缓存
 * DictUtils.loadAll();
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DictUtils {

    /**
     * 字典缓存：dictType -> (dictValue -> dictLabel)
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private static final Map<String, Map<String, String>> DICT_CACHE = new ConcurrentHashMap<>();

    /**
     * 读写锁，保证线程安全
     * <p>
     * 读操作使用读锁，多个线程可同时读取；
     * 写操作使用写锁，保证同一时刻只有一个线程写入
     */
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();


    // ==================== 字典缓存操作 ====================

    /**
     * 获取字典映射
     * <p>
     * 根据字典编码获取对应的 (dictValue -> dictLabel) 映射。
     *
     * @param dictType 字典类型
     * @return 字典值到标签的映射，如果不存在返回空 Map
     */
    public static Map<String, String> getDictMap(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return Collections.emptyMap();
        }
        LOCK.readLock().lock();
        try {
            Map<String, String> map = DICT_CACHE.get(dictType);
            // 返回拷贝，防止外部修改影响缓存
            return ObjectUtil.isNotNull(map) ? new ConcurrentHashMap<>(map) : new ConcurrentHashMap<>();
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * 获取字典标签文本
     * <p>
     * 根据字典类型和字典值查询对应的标签描述。
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @return 对应的字典标签，找不到返回空字符串
     */
    public static String getDictLabel(String dictType, String dictValue) {
        if (StrUtil.isBlank(dictType)) {
            return "";
        }
        LOCK.readLock().lock();
        try {
            Map<String, String> map = DICT_CACHE.get(dictType);
            return ObjectUtil.isNotNull(map) ? map.getOrDefault(dictValue, "") : "";
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * 获取字典值
     * <p>
     * 根据字典类型和字典标签反向查询对应的字典值。
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 对应的字典值，找不到返回空字符串
     */
    public static String getDictValue(String dictType, String dictLabel) {
        if (StrUtil.isBlank(dictType) || StrUtil.isBlank(dictLabel)) {
            return "";
        }
        LOCK.readLock().lock();
        try {
            Map<String, String> map = DICT_CACHE.get(dictType);
            if (CollUtil.isEmpty(map)) {
                return "";
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (dictLabel.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return "";
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * 获取所有已缓存的字典编码
     *
     * @return 字典编码集合
     */
    public static Set<String> getDictMapKey() {
        LOCK.readLock().lock();
        try {
            return new HashSet<>(DICT_CACHE.keySet());
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * 加载指定字典类型的字典数据到缓存
     * <p>
     * 使用写锁保证线程安全，加载后缓存到 DICT_CACHE 中。
     * 如果缓存已存在则先清空再加载。
     *
     * @param dictType 字典类型
     */
    public static void loadDictMap(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return;
        }
        LOCK.writeLock().lock();
        try {
            // 清空缓存
            DICT_CACHE.remove(dictType);
            // 查询数据库中状态正常的字典数据
            List<DictData> list = Db.lambdaQuery(DictData.class)
                    .eq(DictData::getDictType, dictType)
                    .eq(DictData::getStatus, 1)
                    .list();
            // 转换为 Map 并缓存
            Map<String, String> map = list.stream()
                    .collect(Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (a, b) -> a));
            DICT_CACHE.put(dictType, map);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    /**
     * 加载所有字典数据到缓存
     * <p>
     * 在应用启动时执行，将所有字典数据按 dictType 分组存储。
     * 使用写锁保证线程安全。
     */
    public static void loadAll() {
        LOCK.writeLock().lock();
        try {
            // 清空缓存
            DICT_CACHE.clear();
            // 查询所有状态正常的字典数据
            List<DictData> list = Db.lambdaQuery(DictData.class)
                    .eq(DictData::getStatus, 1)
                    .list();
            // 按字典类型分组，每组内按 dictValue -> dictLabel 映射
            Map<String, Map<String, String>> map = list.stream()
                    .collect(Collectors.groupingBy(DictData::getDictType,
                                    Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (a, b) -> a)
                            )
                    );
            DICT_CACHE.putAll(map);
            log.info("字典缓存加载完成，共 {} 条", list.size());
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    /**
     * 刷新所有字典缓存
     * <p>
     * 等同于清空后重新加载所有字典数据。
     */
    public static void refreshAll() {
        loadAll();
    }

}