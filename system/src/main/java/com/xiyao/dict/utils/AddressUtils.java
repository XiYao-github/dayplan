package com.xiyao.dict.utils;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.SysAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 地区信息缓存管理器
 * <p>
 * 负责管理地址数据缓存，提供地区名称查询、层级结构获取等功能。
 * 使用单例模式实现，确保全局唯一的缓存实例。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>地址缓存管理：通过 code 直接获取地址对象和名称</li>
 *     <li>批量查询：支持一次性查询多个地址的名称</li>
 *     <li>批量填充：支持将列表中对象的地址编码自动填充为名称</li>
 *     <li>层级关系：通过 parentCode 获取子地区列表</li>
 *     <li>线程安全：使用读写锁保证并发安全</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 获取单个地址名称
 * String name = AddressUtils.getInstance().getName(440305);
 *
 * // 批量获取名称映射
 * List<Long> codes = Arrays.asList(440305, 440100, 110000);
 * Map<Long, String> nameMap = AddressUtils.getInstance().getNameMap(codes);
 *
 * // 批量填充到对象（常用于 VO 回显）
 * List<UserVO> users = userService.list();
 * AddressUtils.getInstance().fillName(users, UserVO::getRegionCode, UserVO::setRegionName);
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
public class AddressUtils {

    /**
     * 单例实例，使用饿汉模式确保全局唯一
     */
    private static final AddressUtils INSTANCE = new AddressUtils();

    /**
     * 获取单例实例
     *
     * @return AddressUtils 实例
     */
    public static AddressUtils getInstance() {
        return INSTANCE;
    }

    /**
     * 地址缓存：code -> SysAddress
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全，支持高并发读取
     */
    private final Map<Long, SysAddress> addressCache = new ConcurrentHashMap<>();

    /**
     * 子地区缓存：parentCode -> 子地区列表
     * <p>
     * 用于快速查询某地区的下级地区列表
     */
    private final Map<Long, List<SysAddress>> childrenCache = new ConcurrentHashMap<>();

    /**
     * 读写锁，保证缓存更新时的线程安全
     * <p>
     * 读操作可并发执行，写操作独占
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ==================== 基础查询 ====================

    /**
     * 根据区划代码获取地址对象
     *
     * @param code 区划代码
     * @return 地址对象，找不到返回 null
     */
    public SysAddress getAddress(Long code) {
        return addressCache.getOrDefault(code, null);
    }

    /**
     * 根据区划代码获取地区名称
     *
     * @param code 区划代码
     * @return 地区名称，找不到返回空字符串
     */
    public String getName(Long code) {
        SysAddress address = getAddress(code);
        return address != null ? address.getName() : "";
    }

    // ==================== 批量查询 ====================

    /**
     * 批量获取地址名称映射
     * <p>
     * 根据地址编码列表批量查询对应的名称。
     *
     * @param codes 地址编码列表
     * @return 地址编码到名称的映射Map，key为code，value为name
     */
    public Map<Long, String> getNameMap(Collection<Long> codes) {
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::getName, (a, b) -> a));
    }

    /**
     * 批量获取地址对象映射
     *
     * @param codes 地址编码列表
     * @return 地址编码到地址对象的映射Map
     */
    public Map<Long, SysAddress> getAddressMap(Collection<Long> codes) {
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::getAddress, (a, b) -> a));
    }

    // ==================== 批量填充 ====================

    /**
     * 批量填充地址名称
     * <p>
     * 常见使用场景：将数据库存储的地址编码转换为可读的名称。
     * 例如VO中只存储了regionCode，需要回显regionName时使用此方法。
     *
     * <p>
     * <b>示例：</b>
     * <pre>{@code
     * // 假设 UserVO 有 regionCode 和 regionName 两个字段
     * List<UserVO> users = userService.list();
     * addressUtils.fillName(users, UserVO::getRegionCode, UserVO::setRegionName);
     * }</pre>
     *
     * @param list       待填充的对象列表
     * @param codeGetter 从对象中获取地址编码的函数
     * @param nameSetter 将地址名称设置到对象中的函数
     * @param <T>        对象类型
     */
    public <T> void fillName(Collection<T> list, Function<T, Long> codeGetter, BiConsumer<T, String> nameSetter) {
        if (list.isEmpty()) {
            return;
        }
        // 收集去重后的所有编码
        Set<Long> codes = list.stream()
                .map(codeGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 一次性查询所有名称
        Map<Long, String> nameMap = getNameMap(codes);
        // 回填到每个对象
        list.forEach(item -> {
            Long code = codeGetter.apply(item);
            if (code != null) {
                nameSetter.accept(item, nameMap.getOrDefault(code, ""));
            }
        });
    }

    // ==================== 层级关系 ====================

    /**
     * 获取直接子地区列表
     *
     * @param parentCode 父级区划代码
     * @return 子地区列表，如果无子地区返回空列表
     */
    public List<SysAddress> getChildren(Long parentCode) {
        if (childrenCache.containsKey(parentCode)) {
            return Collections.unmodifiableList(childrenCache.get(parentCode));
        } else {
            return Collections.emptyList();
        }
    }

    public List<SysAddress> getAllChildren(Long parentCode) {
        List<SysAddress> result = new ArrayList<>();
        List<SysAddress> children = getChildren(parentCode);
        for (SysAddress child : children) {
            result.add(child);
            result.addAll(getAllChildren(child.getCode()));
        }
        return result;
    }

    public List<SysAddress> getTree(Long code) {
        List<SysAddress> list = new ArrayList<>();

        // 获取所有后代节点
        List<SysAddress> childrenList = getAllChildren(code);

        // 创建映射
        Map<Long, SysAddress> childrenMap = childrenList.stream().collect(Collectors.toMap(SysAddress::getCode, Function.identity()));

        // 构建父子关系
        for (SysAddress children : childrenList) {
            Long parentCode = children.getParentCode();
            if (Objects.equals(parentCode, code)) {
                list.add(children);
            } else {
                SysAddress parent = childrenMap.get(parentCode);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(children);
                }
            }
        }

        return list;
    }

    // ==================== 缓存管理 ====================

    /**
     * 加载所有地址数据到缓存
     * <p>
     * 在应用启动时执行，将数据库中的地址数据全部加载到内存缓存。
     * 使用写锁保证线程安全，加载完成后输出日志。
     */
    public void loadAll() {
        lock.writeLock().lock();
        try {
            addressCache.clear();
            childrenCache.clear();

            List<SysAddress> list = Db.lambdaQuery(SysAddress.class).list();

            for (SysAddress address : list) {
                // 构建地址主缓存
                addressCache.put(address.getCode(), address);

                // 构建子地区缓存
                List<SysAddress> childList = childrenCache.computeIfAbsent(address.getParentCode(), k -> Collections.synchronizedList(new ArrayList<>()));
                childList.add(address);

            }

            log.info("地区缓存加载完成，共 {} 条", list.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 刷新所有地址缓存
     * <p>
     * 等同于清空后重新加载所有地址数据。
     */
    public void refreshAll() {
        loadAll();
    }
}