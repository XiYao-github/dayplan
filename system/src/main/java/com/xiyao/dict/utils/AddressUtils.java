package com.xiyao.dict.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.xiyao.system.entity.SysAddress;
import com.xiyao.system.vo.AddressVo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
 * 使用静态方法实现，直接通过类名调用。
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
 * String name = AddressUtils.getName(440305);
 *
 * // 批量获取名称映射
 * List<Long> codes = Arrays.asList(440305, 440100, 110000);
 * Map<Long, String> nameMap = AddressUtils.getNameMap(codes);
 *
 * // 批量填充到对象（常用于 VO 回显）
 * List<UserVO> users = userService.list();
 * AddressUtils.fillName(users, UserVO::getRegionCode, UserVO::setRegionName);
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    /**
     * 地址缓存：code -> SysAddress
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全，支持高并发读取
     */
    private static final Map<Long, SysAddress> addressCache = new ConcurrentHashMap<>();

    /**
     * 子地区缓存：parentCode -> 子地区列表
     * <p>
     * 用于快速查询某地区的下级地区列表
     */
    private static final Map<Long, List<SysAddress>> childrenCache = new ConcurrentHashMap<>();

    /**
     * 读写锁，保证缓存更新时的线程安全
     * <p>
     * 读操作可并发执行，写操作独占
     */
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ==================== 基础查询 ====================

    /**
     * 根据区划代码获取地址对象
     *
     * @param code 区划代码
     * @return 地址对象，找不到返回 null
     */
    public static AddressVo getAddress(Long code) {
        if (ObjectUtil.isNull(code)) {
            return null;
        }

        lock.readLock().lock();
        try {
            SysAddress address = addressCache.get(code);
            return ObjectUtil.isNotNull(address) ? toVo(address) : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 根据区划代码获取地区名称
     *
     * @param code 区划代码
     * @return 地区名称，找不到返回空字符串
     */
    public static String getName(Long code) {
        if (ObjectUtil.isNull(code)) {
            return "";
        }
        lock.readLock().lock();
        try {
            SysAddress address = addressCache.get(code);
            return ObjectUtil.isNotNull(address) ? address.getName() : "";
        } finally {
            lock.readLock().unlock();
        }
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
    public static Map<Long, String> getNameMap(Collection<Long> codes) {
        if (CollUtil.isEmpty(codes)) {
            return Collections.emptyMap();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), AddressUtils::getName, (a, b) -> a));
    }

    /**
     * 批量获取地址对象映射
     *
     * @param codes 地址编码列表
     * @return 地址编码到地址对象的映射Map
     */
    public static Map<Long, AddressVo> getAddressMap(Collection<Long> codes) {
        if (CollUtil.isEmpty(codes)) {
            return Collections.emptyMap();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), AddressUtils::getAddress, (a, b) -> a));
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
     * AddressUtils.fillName(users, UserVO::getRegionCode, UserVO::setRegionName);
     * }</pre>
     *
     * @param list       待填充的对象列表
     * @param codeGetter 从对象中获取地址编码的函数
     * @param nameSetter 将地址名称设置到对象中的函数
     * @param <T>        对象类型
     */
    public static <T> void fillName(Collection<T> list, Function<T, Long> codeGetter, BiConsumer<T, String> nameSetter) {
        if (CollUtil.isEmpty(list) || ObjectUtil.isNull(codeGetter) || ObjectUtil.isNull(nameSetter)) {
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
            if (ObjectUtil.isNotNull(code)) {
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
    public static List<AddressVo> getChildren(Long parentCode) {
        if (ObjectUtil.isNull(parentCode)) {
            return Collections.emptyList();
        }
        lock.readLock().lock();
        try {
            List<SysAddress> children = childrenCache.get(parentCode);
            if (CollUtil.isEmpty(children)) {
                return Collections.emptyList();
            }
            return children.stream()
                    .map(AddressUtils::toVo)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有后代地区列表（递归）
     *
     * @param parentCode 父级区划代码
     * @return 所有后代地区列表，包含直接子级和间接子级
     */
    public static List<AddressVo> getAllChildren(Long parentCode) {
        return getAllChildren(parentCode, new HashSet<>());
    }

    /**
     * 递归获取所有后代地区列表
     *
     * @param parentCode 父级区划代码
     * @param visited    已访问的编码集合，用于防止循环引用
     * @return 所有后代地区列表
     */
    private static List<AddressVo> getAllChildren(Long parentCode, Set<Long> visited) {
        List<AddressVo> result = new ArrayList<>();
        if (visited.add(parentCode)) {
            List<AddressVo> children = getChildren(parentCode);
            for (AddressVo child : children) {
                result.add(child);
                result.addAll(getAllChildren(child.getCode(), visited));
            }
        }
        return result;
    }

    /**
     * 获取指定地区的树形结构
     * <p>
     * 返回指定 code 下的所有直接子级构成的树形结构，每个节点包含其子节点列表。
     *
     * @param code 区划代码
     * @return 子地区树形列表，如果无子地区返回空列表
     */
    public static List<AddressVo> getTree(Long code) {
        lock.readLock().lock();
        try {
            List<AddressVo> childrenList = getAllChildren(code);
            if (CollUtil.isEmpty(childrenList)) {
                return Collections.emptyList();
            }

            // 建立 code -> AddressVo 的映射，用于快速查找父节点
            Map<Long, AddressVo> childrenMap = childrenList.stream()
                    .collect(Collectors.toMap(AddressVo::getCode, Function.identity(), (a, b) -> a));

            List<AddressVo> roots = new ArrayList<>();
            for (AddressVo child : childrenList) {
                Long parentCode = child.getParentCode();
                if (Objects.equals(parentCode, code)) {
                    // 顶层节点，直接子级
                    roots.add(child);
                } else {
                    // 非顶层节点，挂到父节点下
                    AddressVo parent = childrenMap.get(parentCode);
                    if (ObjectUtil.isNotNull(parent)) {
                        parent.addChild(child);
                    }
                }
            }
            return roots;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ==================== 缓存管理 ====================

    /**
     * 加载所有地址数据到缓存
     * <p>
     * 在应用启动时执行，将数据库中的地址数据全部加载到内存缓存。
     * 使用写锁保证线程安全，加载完成后输出日志。
     */
    public static void loadAll() {
        lock.writeLock().lock();
        try {
            addressCache.clear();
            childrenCache.clear();

            List<SysAddress> list = Db.lambdaQuery(SysAddress.class).list();

            for (SysAddress address : list) {
                addressCache.put(address.getCode(), address);
                childrenCache.computeIfAbsent(address.getParentCode(), k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(address);
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
    public static void refreshAll() {
        loadAll();
    }

    /**
     * 将 SysAddress 实体转换为 AddressVo 视图对象
     *
     * @param source 实体对象
     * @return 视图对象，如果实体为 null 则返回 null
     */
    private static AddressVo toVo(SysAddress source) {
        if (ObjectUtil.isNull(source)) {
            return null;
        }
        return new AddressVo()
                .setCode(source.getCode())
                .setParentCode(source.getParentCode())
                .setName(source.getName())
                .setProvinceCode(source.getProvinceCode())
                .setProvinceName(source.getProvinceName())
                .setCityCode(source.getCityCode())
                .setCityName(source.getCityName())
                .setAreaCode(source.getAreaCode())
                .setAreaName(source.getAreaName())
                .setSort(source.getSort())
                .setLevel(source.getLevel());
    }
}