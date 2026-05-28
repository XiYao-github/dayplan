package com.xiyao.dict.utils;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.DictData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 数据字典缓存管理器
 * <p>
 * 负责管理字典数据缓存，提供字典标签查询功能。
 * 使用单例模式实现，确保全局唯一的缓存实例。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>字典缓存管理：存储 dictCode -> (dictValue -> dictLabel) 的映射</li>
 *     <li>线程安全：使用读写锁保证并发安全</li>
 *     <li>全量加载：应用启动时加载所有字典数据到缓存</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 获取字典标签
 * String label = DictCache.getInstance().getDictLabel("status", "1");
 *
 * // 刷新字典缓存
 * DictCache.getInstance().loadDictAll();
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
public class DictCache {

    /**
     * 单例实例
     * <p>
     * 使用饿汉模式，确保全局唯一实例
     */
    private static final DictCache INSTANCE = new DictCache();

    /**
     * 获取单例实例
     *
     * @return DictCache 实例
     */
    public static DictCache getInstance() {
        return INSTANCE;
    }

    /**
     * 字典缓存：dictCode -> (dictValue -> dictLabel)
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<String, Map<String, String>> dictCache = new ConcurrentHashMap<>();

    /**
     * 读写锁，保证线程安全
     * <p>
     * 读操作使用读锁，多个线程可同时读取；
     * 写操作使用写锁，保证同一时刻只有一个线程写入
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    // ==================== 字典缓存操作 ====================

    /**
     * 获取字典映射
     * <p>
     * 根据字典编码获取对应的 (dictValue -> dictLabel) 映射。
     *
     * @param dictCode 字典编码
     * @return 字典值到标签的映射，如果不存在返回空 Map
     */
    public Map<String, String> getDictMap(String dictCode) {
        return dictCache.getOrDefault(dictCode, new ConcurrentHashMap<>());
    }

    /**
     * 获取字典标签文本
     * <p>
     * 根据字典编码和字典值查询对应的标签描述。
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return 对应的字典标签，找不到返回空字符串
     */
    public String getDictLabel(String dictCode, String dictValue) {
        Map<String, String> map = getDictMap(dictCode);
        return map.getOrDefault(dictValue, "");
    }

    /**
     * 获取所有已缓存的字典编码
     *
     * @return 字典编码集合
     */
    public Set<String> getDictMapKey() {
        return new HashSet<>(dictCache.keySet());
    }

    /**
     * 加载指定字典编码的字典数据到缓存
     * <p>
     * 使用写锁保证线程安全，加载后缓存到 dictCache 中。
     * 如果缓存已存在则先清空再加载。
     *
     * @param dictCode 字典编码
     */
    public void loadDictMap(String dictCode) {
        lock.writeLock().lock();
        try {
            // 清空缓存
            if (dictCache.containsKey(dictCode)) {
                dictCache.remove(dictCode);
            }
            // 查询数据库中状态正常的字典数据
            List<DictData> list = Db.lambdaQuery(DictData.class)
                    .eq(DictData::getDictCode, dictCode)
                    .eq(DictData::getStatus, 1)
                    .list();
            // 转换为 Map 并缓存
            Map<String, String> map = list.stream()
                    .collect(Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (v1, v2) -> v1));
            dictCache.put(dictCode, map);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 加载所有字典数据到缓存
     * <p>
     * 在应用启动时执行，将所有字典数据按 dictCode 分组存储。
     * 使用写锁保证线程安全。
     */
    public void loadDictAll() {
        lock.writeLock().lock();
        try {
            // 清空缓存
            if (!dictCache.isEmpty()) {
                dictCache.clear();
            }
            // 查询所有状态正常的字典数据
            List<DictData> list = Db.lambdaQuery(DictData.class)
                    .eq(DictData::getStatus, 1)
                    .list();
            // 按字典编码分组，每组内按 dictValue -> dictLabel 映射
            Map<String, Map<String, String>> map = list.stream()
                    .collect(Collectors.groupingBy(DictData::getDictCode,
                                    Collectors.toMap(DictData::getDictValue, DictData::getDictLabel, (v1, v2) -> v1)
                            )
                    );
            dictCache.putAll(map);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 刷新所有字典缓存
     * <p>
     * 等同于清空后重新加载所有字典数据。
     */
    public void refreshAll() {
        loadDictAll();
    }

}