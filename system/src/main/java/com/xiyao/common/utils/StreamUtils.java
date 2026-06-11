package com.xiyao.common.utils;

import cn.hutool.core.collection.CollUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Stream 流工具类
 * <p>
 * 封装 Java 8 Stream API 的常用操作，提供集合的过滤、转换、聚合等功能。
 * 所有方法均为静态工具方法，通过 {@link Collectors} 实现各种集合操作。
 *
 * <p>
 * <b>主要功能：</b>
 * <ul>
 *     <li>过滤：filter - 保留满足条件的元素</li>
 *     <li>查找：findFirst/findAny - 查找满足条件的元素</li>
 *     <li>转换：toList/toSet/toMap - 类型转换</li>
 *     <li>聚合：join/sorted - 字符串拼接和排序</li>
 *     <li>分组：groupByKey/groupBy2Key - 按规则分组</li>
 *     <li>合并：merge - 合并两个 Map</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 过滤
 * List<User> admins = StreamUtils.filter(users, u -> u.getAdminType() == 1);
 *
 * // 查找
 * User first = StreamUtils.findFirstValue(users, u -> u.getAge() > 18);
 *
 * // 转换
 * List<String> names = StreamUtils.toList(users, User::getName);
 *
 * // 分组
 * Map<Integer, List<User>> group = StreamUtils.groupByKey(users, User::getDeptId);
 * }</pre>
 *
 * @author Lion Li
 * @see Collectors
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtils {

    /**
     * 分隔符常量
     * <p>
     * 用于 join 等方法的默认分隔符，默认为英文逗号。
     */
    public static final String SEPARATOR = ",";

    /**
     * 将集合过滤，只保留满足条件的元素
     * <p>
     * 使用 Predicate 接口进行条件过滤，返回过滤后的新列表。
     * 如果原集合为空或 null，返回空列表而非 null。
     *
     * @param collection 需要过滤的集合
     * @param function   过滤条件，参数为集合元素，返回 true 保留，false 丢弃
     * @param <E>        集合元素类型
     * @return 过滤后的新列表，永远不会返回 null
     */
    public static <E> List<E> filter(Collection<E> collection, Predicate<E> function) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        return collection.stream()
                .filter(function)
                // 注意此处不要使用 .toList() 新语法
                // 因为 toList() 返回的是不可变 List，会导致序列化问题（如 Redis 存储、Jackson 序列化）
                // 故使用 Collectors.toList() 显式创建可变 ArrayList
                .collect(Collectors.toList());
    }

    /**
     * 找到流中满足条件的第一个元素
     * <p>
     * 使用 findFirst() 返回 Stream 中的第一个匹配元素，以 Optional 包装。
     * 如果没有元素满足条件，返回 Optional.empty()。
     * <p>
     * 注意：findFirst() 不保证在并行流中的性能，如果对顺序没有要求可以使用 findAny()。
     *
     * @param collection 需要查询的集合
     * @param function   过滤条件，参数为集合元素，返回 true 表示匹配
     * @param <E>        集合元素类型
     * @return 满足条件的第一个元素，以 Optional 包装；无匹配时返回 Optional.empty()
     * @see #findFirstValue(Collection, Predicate)
     */
    public static <E> Optional<E> findFirst(Collection<E> collection, Predicate<E> function) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(function)
                .findFirst();
    }

    /**
     * 找到流中满足条件的第一个元素值
     * <p>
     * 与 {@link #findFirst(Collection, Predicate)} 类似，但直接返回元素值而非 Optional。
     * 如果没有匹配元素，返回 null。
     *
     * @param collection 需要查询的集合
     * @param function   过滤条件
     * @param <E>        集合元素类型
     * @return 满足条件的第一个元素值，无匹配时返回 null
     * @see #findFirst(Collection, Predicate)
     */
    public static <E> E findFirstValue(Collection<E> collection, Predicate<E> function) {
        return findFirst(collection, function).orElse(null);
    }

    /**
     * 找到流中任意一个满足条件的元素
     * <p>
     * 使用 findAny() 返回 Stream 中的任意一个匹配元素（不保证是第一个）。
     * 在串行流中与 findFirst() 行为相同；在并行流中，findAny() 可能有更好的性能。
     *
     * @param collection 需要查询的集合
     * @param function   过滤条件
     * @param <E>        集合元素类型
     * @return 满足条件的任意一个元素，以 Optional 包装；无匹配时返回 Optional.empty()
     * @see #findAnyValue(Collection, Predicate)
     */
    public static <E> Optional<E> findAny(Collection<E> collection, Predicate<E> function) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(function)
                .findAny();
    }

    /**
     * 找到流中任意一个满足条件的元素值
     * <p>
     * 与 {@link #findAny(Collection, Predicate)} 类似，但直接返回元素值而非 Optional。
     * 如果没有匹配元素，返回 null。
     *
     * @param collection 需要查询的集合
     * @param function   过滤条件
     * @param <E>        集合元素类型
     * @return 满足条件的任意一个元素值，无匹配时返回 null
     * @see #findAny(Collection, Predicate)
     */
    public static <E> E findAnyValue(Collection<E> collection, Predicate<E> function) {
        return findAny(collection, function).orElse(null);
    }

    /**
     * 将集合中的元素通过函数转换后，用分隔符拼接成字符串
     * <p>
     * 等同于调用 join(collection, function, SEPARATOR)，使用默认逗号分隔符。
     * 转换时自动过滤 null 值。
     *
     * @param collection 需要转换的集合
     * @param function   转换函数，将元素转为 String
     * @param <E>        集合元素类型
     * @return 拼接后的字符串，如果集合为空返回空字符串
     * @see #join(Collection, Function, CharSequence)
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function) {
        return join(collection, function, SEPARATOR);
    }

    /**
     * 将集合中的元素通过函数转换后，用指定分隔符拼接成字符串
     * <p>
     * 示例：join(users, User::getName, "-") 返回 "张三-李四-王五"
     * 转换时自动过滤 null 值，只拼接非 null 的结果。
     *
     * @param collection 需要转换的集合
     * @param function   转换函数，将元素转为 String
     * @param delimiter  分隔符，如 "-"、","、" | "
     * @param <E>        集合元素类型
     * @return 拼接后的字符串，如果集合为空返回空字符串
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function, CharSequence delimiter) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return StringUtils.EMPTY;
        }
        return collection.stream()
                .map(function)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 将集合按指定比较器排序后返回新列表
     * <p>
     * 执行稳定排序（相等元素的相对顺序保持不变）。
     * 自动过滤 null 值和空集合。
     *
     * @param collection 需要排序的集合
     * @param comparing  比较器，定义排序规则
     * @param <E>        集合元素类型
     * @return 排序后的新列表，空集合返回空列表
     */
    public static <E> List<E> sorted(Collection<E> collection, Comparator<E> comparing) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .sorted(comparing)
                // 注意此处不要使用 .toList() 新语法，因为返回的是不可变 List，会导致序列化问题
                .collect(Collectors.toList());
    }

    /**
     * 将集合转换为 Map，key 和 value 直接映射（值类型不变）
     * <p>
     * 转换关系：{@code Collection<V> → Map<K,V>}
     * key 函数用于生成 Map 的 key，value 直接使用原集合元素。
     * 如果存在重复 key，后面的值会覆盖前面的值（保留 (l, r) → l）。
     *
     * @param collection 需要转换的集合
     * @param key        将元素 V 转为 key K 的函数
     * @param <V>        原集合中的元素类型（也是 Map 的 value 类型）
     * @param <K>        Map 的 key 类型
     * @return 转换后的 Map，key 为指定的 K 类型，value 为 V 类型
     * @throws NullPointerException 如果 key 为 null
     */
    public static <V, K> Map<K, V> toIdentityMap(Collection<V> collection, Function<V, K> key) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(key, Function.identity(), (l, r) -> l));
    }

    /**
     * 将集合转换为 Map，key 和 value 可以是不同的类型
     * <p>
     * 转换关系：{@code Collection<E> → Map<K,V>}
     * 不同于 toIdentityMap，这里 value 也可以通过 function 转换得到。
     * 如果存在重复 key，后面的值会覆盖前面的值。
     *
     * @param collection 需要转换的集合
     * @param key        将元素 E 转为 key K 的函数
     * @param value      将元素 E 转为 value V 的函数
     * @param <E>        原集合中的元素类型
     * @param <K>        Map 的 key 类型
     * @param <V>        Map 的 value 类型
     * @return 转换后的 Map
     * @throws NullPointerException 如果 key 或 value 函数返回 null
     */
    public static <E, K, V> Map<K, V> toMap(Collection<E> collection, Function<E, K> key, Function<E, V> value) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(key, value, (l, r) -> l));
    }

    /**
     * 将已有 Map 的 value 通过函数转换，生成新 Map（key 不变）
     * <p>
     * 遍历原 Map，对每个 entry 应用 take 函数生成新的 value。
     * key 保持不变，value 类型可能发生变化。
     *
     * @param map  需要处理的原 Map
     * @param take 取值函数，参数为 (key, oldValue)，返回 newValue
     * @param <K>  Map 的 key 类型
     * @param <E>  原 Map 的 value 类型
     * @param <V>  新 Map 的 value 类型
     * @return 新的 Map，key 不变，value 为转换后的结果
     */
    public static <K, E, V> Map<K, V> toMap(Map<K, E> map, BiFunction<K, E, V> take) {
        // 空 Map 校验
        if (CollUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }
        // 使用 toMap 重载版本进行转换
        return toMap(map.entrySet(), Map.Entry::getKey, entry -> take.apply(entry.getKey(), entry.getValue()));
    }

    /**
     * 将集合按 key 函数规则分组为 Map
     * <p>
     * 转换关系：{@code Collection<E> → Map<K,List<E>>}
     * 具有相同 key 值的元素会被归类到同一个 List 中。
     * 使用 LinkedHashMap 保持分组后的顺序与原集合一致。
     *
     * @param collection 需要分类的集合
     * @param key        分类规则函数，将元素转为 key
     * @param <E>        集合元素类型
     * @param <K>        Map 的 key 类型（分组依据）
     * @return 分组后的 Map，key 为分类键，value 为该分类下的元素列表
     */
    public static <E, K> Map<K, List<E>> groupByKey(Collection<E> collection, Function<E, K> key) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 将集合按两个 key 函数规则分组为双层 Map
     * <p>
     * 转换关系：{@code Collection<E> → Map<K1,Map<K2,List<E>>}
     * 先按 key1 分组，每个分组内再按 key2 分组。
     * 用于需要两级分类的场景，如：按年级分组，再按班级分组。
     *
     * @param collection 需要分类的集合
     * @param key1       第一级分类规则
     * @param key2       第二级分类规则
     * @param <E>        集合元素类型
     * @param <K>        第一层 Map 的 key 类型
     * @param <U>        第二层 Map 的 key 类型
     * @return 双层分组后的 Map
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(Collection<E> collection, Function<E, K> key1, Function<E, U> key2) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.groupingBy(key2, LinkedHashMap::new, Collectors.toList())));
    }

    /**
     * 将集合按两个 key 函数规则分组，每组只保留一个元素（后覆盖前）
     * <p>
     * 转换关系：{@code Collection<E> → Map<K1,Map<K2,E>>}
     * 与 groupBy2Key 类似，但每组只保留一个元素（通常用于去重场景）。
     * 如果同一个二级 key 有多个元素，后面的会覆盖前面的。
     *
     * @param collection 需要分类的集合
     * @param key1       第一级分类规则
     * @param key2       第二级分类规则
     * @param <E>        集合元素类型
     * @param <T>        第一层 Map 的 key 类型
     * @param <U>        第二层 Map 的 key 类型
     * @return 双层分组后的 Map，每组只有一个元素
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(Collection<E> collection, Function<E, T> key1, Function<E, U> key2) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.toMap(key2, Function.identity(), (l, r) -> l)));
    }

    /**
     * 将集合转换为另一种类型的 List
     * <p>
     * 转换关系：{@code Collection<E> → List<T>}
     * 通过 function 将 E 类型转换为 T 类型。
     * 转换时自动过滤 null 值。
     *
     * @param collection 需要转换的集合
     * @param function   转换函数，将 E 类型转为 T 类型
     * @param <E>        原集合的元素类型
     * @param <T>        目标 List 的元素类型
     * @return 转换后的 List
     */
    public static <E, T> List<T> toList(Collection<E> collection, Function<E, T> function) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        return collection.stream()
                .map(function)
                .filter(Objects::nonNull)
                // 注意此处不要使用 .toList() 新语法，因为返回的是不可变 List，会导致序列化问题
                .collect(Collectors.toList());
    }

    /**
     * 将集合转换为另一种类型的 Set
     * <p>
     * 转换关系：{@code Collection<E> → Set<T>}
     * 通过 function 将 E 类型转换为 T 类型。
     * 转换时自动过滤 null 值，结果会去重（Set 特性）。
     *
     * @param collection 需要转换的集合
     * @param function   转换函数，将 E 类型转为 T 类型
     * @param <E>        原集合的元素类型
     * @param <T>        目标 Set 的元素类型
     * @return 转换后的 Set（可能包含 null 值被过滤）
     */
    public static <E, T> Set<T> toSet(Collection<E> collection, Function<E, T> function) {
        // 空集合校验
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newHashSet();
        }
        return collection.stream()
                .map(function)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 合并两个相同 key 类型的 Map
     * <p>
     * 将 map1 和 map2 按 key 合并，value 通过 merge 函数处理。
     * 如果某个 key 只在一个 map 中存在，另一个的 value 传入 null。
     * 用于合并配置、分组聚合等场景。
     *
     * @param map1  第一个需要合并的 Map
     * @param map2  第二个需要合并的 Map
     * @param merge 合并函数，参数为 (value1, value2)，返回合并后的 value
     * @param <K>   Map 的 key 类型
     * @param <X>   第一个 Map 的 value 类型
     * @param <Y>   第二个 Map 的 value 类型
     * @param <V>   最终 Map 的 value 类型
     * @return 合并后的 Map
     */
    public static <K, X, Y, V> Map<K, V> merge(Map<K, X> map1, Map<K, Y> map2, BiFunction<X, Y, V> merge) {
        // 1. 处理两个 Map 都为空的情况
        if (CollUtil.isEmpty(map1) && CollUtil.isEmpty(map2)) {
            return Collections.emptyMap();
        }
        // 2. 处理 map1 为空，map2 非空的情况
        else if (CollUtil.isEmpty(map1)) {
            // map2 的 value 传入 merge 函数，map1 的 value 传入 null
            return toMap(map2.entrySet(), Map.Entry::getKey, entry -> merge.apply(null, entry.getValue()));
        }
        // 3. 处理 map2 为空，map1 非空的情况
        else if (CollUtil.isEmpty(map2)) {
            // map1 的 value 传入 merge 函数，map2 的 value 传入 null
            return toMap(map1.entrySet(), Map.Entry::getKey, entry -> merge.apply(entry.getValue(), null));
        }
        // 4. 两个 Map 都非空，合并所有 key
        // 收集两个 Map 的所有 key
        Set<K> keySet = new HashSet<>();
        keySet.addAll(map1.keySet());
        keySet.addAll(map2.keySet());
        // 对每个 key 执行 merge 函数
        return toMap(keySet, key -> key, key -> merge.apply(map1.get(key), map2.get(key)));
    }

}