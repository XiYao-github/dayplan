package com.xiyao.dict.config;

import com.xiyao.common.utils.SpringUtils;
import com.xiyao.dict.service.DictService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 数据字典管理器
 * <p>
 * 负责缓存字典数据，支持本地缓存和刷新。
 * 使用单例模式实现，确保全局唯一的缓存实例。
 *
 * <p>
 * <b>核心功能：</b>
 * <ol>
 *     <li>字典缓存管理：存储 dictCode -> (dictValue -> dictLabel) 的映射</li>
 *     <li>延迟加载：首次查询时自动加载字典数据</li>
 *     <li>缓存刷新：支持手动刷新指定字典或全部字典</li>
 *     <li>线程安全：使用读写锁保证并发安全</li>
 * </ol>
 *
 * <p>
 * <b>注意：</b> 本类已迁移至 DictCache，推荐使用 DictCache 代替。
 * DictCache 在此基础上增加了枚举缓存功能。
 *
 * @author xiyao
 * @deprecated 推荐使用 {@link DictCache}，该类保留用于兼容旧代码
 * @see DictCache
 */
@Slf4j
public class DictManager {

    /**
     * 单例实例
     * <p>
     * 使用饿汉模式，确保全局唯一实例
     */
    private static final DictManager INSTANCE = new DictManager();

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

    /**
     * 私有构造函数，防止外部实例化
     */
    private DictManager() {
    }

    /**
     * 获取单例实例
     *
     * @return DictManager 实例
     */
    public static DictManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取字典Map（dictValue -> dictLabel）
     * <p>
     * 如果缓存不存在则先从数据库加载。
     *
     * @param dictCode 字典编码
     * @return 字典值到标签的映射，如果不存在返回空Map
     */
    public Map<String, String> getDictMap(String dictCode) {
        // 先从缓存获取
        Map<String, String> map = dictCache.get(dictCode);
        if (map == null) {
            // 缓存未命中，加载字典
            loadDictMap(dictCode);
            map = dictCache.get(dictCode);
        }
        return map != null ? map : new ConcurrentHashMap<>();
    }

    /**
     * 加载指定字典编码的字典数据
     * <p>
     * 使用写锁保证线程安全，加载后缓存到 dictCache 中。
     * 使用双重检查锁定避免重复加载。
     *
     * @param dictCode 字典编码
     */
    public void loadDictMap(String dictCode) {
        lock.writeLock().lock();
        try {
            // 双重检查：避免重复加载
            if (dictCache.containsKey(dictCode)) {
                return;
            }
            // 从数据库加载
            try {
                DictService dictService = SpringUtils.getBean(DictService.class);
                Map<String, String> map = dictService.getDictMap(dictCode);
                dictCache.put(dictCode, map);
                log.info("字典缓存加载成功: dictCode={}, count={}", dictCode, map.size());
            } catch (Exception e) {
                log.warn("字典缓存加载失败: dictCode={}, error={}", dictCode, e.getMessage());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 加载所有字典数据到缓存
     * <p>
     * 将所有字典数据按 dictCode 分组存储。
     */
    public void loadAllDictMap() {
        lock.writeLock().lock();
        try {
            try {
                DictService dictService = SpringUtils.getBean(DictService.class);
                List<com.xiyao.system.entity.DictData> allData = dictService.getAllDictData();
                // 按字典类型分组
                allData.forEach(dictData -> {
                    String dictCode = dictData.getDictCode();
                    dictCache.computeIfAbsent(dictCode, k -> new ConcurrentHashMap<>())
                            .put(dictData.getDictValue(), dictData.getDictLabel());
                });
                log.info("字典缓存全量加载成功: dictCodeCount={}, totalCount={}", dictCache.size(), allData.size());
            } catch (Exception e) {
                log.warn("字典缓存全量加载失败: error={}", e.getMessage());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 刷新字典缓存
     * <p>
     * 清空所有字典缓存后重新从数据库加载全部字典数据。
     */
    public void refresh() {
        lock.writeLock().lock();
        try {
            dictCache.clear();
            loadAllDictMap();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取缓存的字典编码列表
     *
     * @return 字典编码集合
     */
    public java.util.Set<String> getCachedDictCodes() {
        return new java.util.HashSet<>(dictCache.keySet());
    }

    /**
     * 判断指定字典编码是否已缓存
     *
     * @param dictCode 字典编码
     * @return true 表示已缓存，false 表示未缓存
     */
    public boolean isCached(String dictCode) {
        return dictCache.containsKey(dictCode);
    }
}