package com.xiyao.dict.config;

import com.xiyao.common.utils.SpringUtils;
import com.xiyao.dict.enums.BaseEnum;
import com.xiyao.dict.service.DictService;
import com.xiyao.system.entity.DictData;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 数据字典和枚举缓存管理器
 * <p>
 * 项目启动时加载所有字典数据和枚举类型，提供全局查询接口。
 * 使用单例模式实现，确保全局唯一的缓存实例。
 *
 * <p>
 * <b>核心功能：</b>
 * <ol>
 *     <li>字典缓存管理：存储 dictCode -> (dictValue -> dictLabel) 的映射</li>
 *     <li>枚举缓存管理：提供按 code/desc/name 三种方式查询枚举</li>
 *     <li>线程安全：使用读写锁保证并发安全</li>
 *     <li>延迟加载：首次访问时自动加载，支持预加载模式</li>
 * </ol>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 获取字典标签
 * String label = DictCache.getInstance().getDictLabel("status", "1");
 *
 * // 按code查询枚举
 * DataStatus status = DictCache.getInstance().getEnumByCode(DataStatus.class, "1");
 *
 * // 按描述查询枚举
 * DataStatus status = DictCache.getInstance().getEnumByDesc(DataStatus.class, "正常");
 *
 * // 刷新字典缓存
 * DictCache.getInstance().refreshDict("status");
 * }</pre>
 *
 * @author xiyao
 * @see BaseEnum
 * @see DictService
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
     * 字典缓存：dictCode -> (dictValue -> dictLabel)
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<String, Map<String, String>> dictCache = new ConcurrentHashMap<>();

    /**
     * 枚举缓存：按 code 值查询（code字符串 -> 枚举常量）
     * <p>
     * 用于支持 DictEnumConverterFactory 的按 code 转换功能
     */
    private final Map<Class<? extends BaseEnum<?>>, Map<String, ? extends BaseEnum<?>>> enumByCodeCache = new ConcurrentHashMap<>();

    /**
     * 枚举缓存：按描述查询（描述 -> 枚举常量）
     * <p>
     * 用于支持 DictEnumConverterFactory 的按 desc 转换功能
     */
    private final Map<Class<? extends BaseEnum<?>>, Map<String, ? extends BaseEnum<?>>> enumByDescCache = new ConcurrentHashMap<>();

    /**
     * 枚举缓存：按名字查询（枚举名 -> 枚举常量）
     * <p>
     * 用于支持 DictEnumConverterFactory 的按 name 转换功能
     */
    private final Map<Class<? extends BaseEnum<?>>, Map<String, ? extends BaseEnum<?>>> enumByNameCache = new ConcurrentHashMap<>();

    /**
     * 已加载的枚举类型集合
     * <p>
     * 记录已加载的枚举类型，避免重复加载
     */
    private final Set<Class<? extends BaseEnum<?>>> loadedEnumTypes = ConcurrentHashMap.newKeySet();

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
    private DictCache() {
    }

    /**
     * 获取单例实例
     *
     * @return DictCache 实例
     */
    public static DictCache getInstance() {
        return INSTANCE;
    }

    // ==================== 字典缓存操作 ====================

    /**
     * 获取字典Map（dictValue -> dictLabel）
     * <p>
     * 如果缓存不存在则先从数据库加载。
     * 使用双重检查锁定保证线程安全。
     *
     * @param dictCode 字典编码
     * @return 字典值到标签的映射，如果不存在返回空Map
     */
    public Map<String, String> getDictMap(String dictCode) {
        Map<String, String> map = dictCache.get(dictCode);
        if (map == null) {
            // 缓存未命中，调用 loadDictMap 加载
            loadDictMap(dictCode);
            map = dictCache.get(dictCode);
        }
        // 返回时确保不为 null
        return map != null ? map : new ConcurrentHashMap<>();
    }

    /**
     * 获取字典标签文本
     * <p>
     * 根据字典编码和字典值查询对应的标签描述。
     * 如果找不到对应的标签，返回空字符串。
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
     * 加载指定字典编码的字典数据到缓存
     * <p>
     * 使用写锁保证线程安全，加载后缓存到 dictCache 中。
     * 如果缓存已存在则跳过加载。
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
            try {
                // 从数据库加载字典数据
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
     * 在应用启动时或调用 refreshAllDict 时执行。
     * 将所有字典数据按 dictCode 分组存储。
     */
    public void loadAllDictData() {
        lock.writeLock().lock();
        try {
            try {
                DictService dictService = SpringUtils.getBean(DictService.class);
                List<DictData> allData = dictService.getAllDictData();
                // 按字典类型分组，构建 dictCode -> (dictValue -> dictLabel) 的映射
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
     * 刷新指定字典编码的缓存
     * <p>
     * 删除指定字典的缓存后重新从数据库加载。
     * 用于管理员在后台修改字典数据后手动刷新。
     *
     * @param dictCode 字典编码
     */
    public void refreshDict(String dictCode) {
        lock.writeLock().lock();
        try {
            // 先移除缓存
            dictCache.remove(dictCode);
            // 重新加载
            loadDictMap(dictCode);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 刷新所有字典缓存
     * <p>
     * 清空所有字典缓存后重新从数据库加载全部字典数据。
     * 通常用于数据源变更后全量刷新。
     */
    public void refreshAllDict() {
        lock.writeLock().lock();
        try {
            dictCache.clear();
            loadAllDictData();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== 枚举缓存操作 ====================

    /**
     * 获取枚举常量（按 code 值查询）
     * <p>
     * 通过枚举的 getCode() 返回值查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param code     存储值（getCode 返回值）
     * @param <T>      枚举类型
     * @return 对应的枚举常量，找不到返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> T getEnumByCode(Class<T> enumType, String code) {
        // 确保枚举已加载到缓存
        ensureEnumCached(enumType);
        Map<String, T> cache = (Map<String, T>) enumByCodeCache.get(enumType);
        if (cache == null) {
            return null;
        }
        return cache.get(code);
    }

    /**
     * 获取枚举常量（按描述查询）
     * <p>
     * 通过枚举的 getDesc() 返回值查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param desc     描述文本（getDesc 返回值）
     * @param <T>      枚举类型
     * @return 对应的枚举常量，找不到返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> T getEnumByDesc(Class<T> enumType, String desc) {
        ensureEnumCached(enumType);
        Map<String, T> cache = (Map<String, T>) enumByDescCache.get(enumType);
        if (cache == null) {
            return null;
        }
        return cache.get(desc);
    }

    /**
     * 获取枚举常量（按枚举名字查询）
     * <p>
     * 通过枚举常量的名称（如 NORMAL）查询对应的枚举常量。
     *
     * @param enumType 枚举类型
     * @param name     枚举常量名（如 NORMAL）
     * @param <T>      枚举类型
     * @return 对应的枚举常量，找不到返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> T getEnumByName(Class<T> enumType, String name) {
        ensureEnumCached(enumType);
        Map<String, T> cache = (Map<String, T>) enumByNameCache.get(enumType);
        if (cache == null) {
            return null;
        }
        return cache.get(name);
    }

    /**
     * 获取枚举的所有 code 值列表
     * <p>
     * 返回该枚举类型所有 code 值的列表，常用于下拉选项。
     *
     * @param enumType 枚举类型
     * @param <T>      枚举类型
     * @return 该枚举类型所有 code 值的列表
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> List<String> getEnumCodes(Class<T> enumType) {
        ensureEnumCached(enumType);
        Map<String, T> cache = (Map<String, T>) enumByCodeCache.get(enumType);
        if (cache == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cache.keySet());
    }

    /**
     * 加载枚举类型到缓存
     * <p>
     * 构建三个索引：
     * <ol>
     *     <li>enumByCodeCache：按 code 值索引，用于按存储值查询</li>
     *     <li>enumByDescCache：按描述索引，用于按描述文本查询</li>
     *     <li>enumByNameCache：按名字索引，用于按枚举名称查询</li>
     * </ol>
     * 如果枚举已加载则跳过。
     *
     * @param enumType 枚举类型
     * @param <T>      枚举类型
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> void loadEnum(Class<T> enumType) {
        lock.writeLock().lock();
        try {
            // 避免重复加载
            if (loadedEnumTypes.contains(enumType)) {
                return;
            }

            T[] constants = enumType.getEnumConstants();
            Map<String, T> byCode = new ConcurrentHashMap<>();
            Map<String, T> byDesc = new ConcurrentHashMap<>();
            Map<String, T> byName = new ConcurrentHashMap<>();

            // 遍历枚举常量，构建三个索引
            for (T constant : constants) {
                String code = constant.getCode().toString();
                String desc = constant.getDesc();
                String name = ((Enum<?>) constant).name();

                byCode.put(code, constant);
                byDesc.put(desc, constant);
                byName.put(name, constant);
            }

            // 存入三个索引缓存
            enumByCodeCache.put((Class<BaseEnum<?>>) enumType, byCode);
            enumByDescCache.put((Class<BaseEnum<?>>) enumType, byDesc);
            enumByNameCache.put((Class<BaseEnum<?>>) enumType, byName);
            loadedEnumTypes.add(enumType);

            log.info("枚举缓存加载成功: enumType={}, count={}", enumType.getSimpleName(), constants.length);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 确保枚举已加载到缓存
     * <p>
     * 如果尚未加载则调用 loadEnum 加载。
     * 这是内部使用的延迟加载方法。
     *
     * @param enumType 枚举类型
     * @param <T>      枚举类型
     */
    private <T extends BaseEnum<?>> void ensureEnumCached(Class<T> enumType) {
        if (!loadedEnumTypes.contains(enumType)) {
            loadEnum(enumType);
        }
    }

    /**
     * 刷新指定枚举类型的缓存
     * <p>
     * 移除该枚举的缓存后重新加载。
     * 用于修改枚举定义后手动刷新。
     *
     * @param enumType 枚举类型
     */
    public void refreshEnum(Class<? extends BaseEnum<?>> enumType) {
        lock.writeLock().lock();
        try {
            // 移除缓存记录和三个索引缓存
            loadedEnumTypes.remove(enumType);
            enumByCodeCache.remove(enumType);
            enumByDescCache.remove(enumType);
            enumByNameCache.remove(enumType);
            // 重新加载
            loadEnum(enumType);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 刷新所有枚举类型的缓存
     * <p>
     * 对所有已加载的枚举类型分别执行 refreshEnum 操作。
     */
    public void refreshAllEnums() {
        lock.writeLock().lock();
        try {
            loadedEnumTypes.forEach(this::refreshEnum);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== 全量刷新 ====================

    /**
     * 刷新所有缓存（字典+枚举）
     * <p>
     * 调用 refreshAllDict 和 refreshAllEnums，
     * 用于全局刷新缓存场景。
     */
    public void refreshAll() {
        refreshAllDict();
        refreshAllEnums();
    }

    /**
     * 获取所有已缓存的字典编码
     *
     * @return 字典编码集合
     */
    public Set<String> getCachedDictCodes() {
        return new HashSet<>(dictCache.keySet());
    }

    /**
     * 获取所有已缓存的枚举类型
     *
     * @return 枚举类型集合
     */
    public Set<Class<? extends BaseEnum<?>>> getCachedEnumTypes() {
        return new HashSet<>(loadedEnumTypes);
    }
}