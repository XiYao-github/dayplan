package com.xiyao.common.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 对象转换工具类
 * <p>
 * 封装 Spring BeanUtils，提供对象属性拷贝的便捷方法。
 * 用于 DTO/VO/Entity 之间的属性复制，避免手动 set 的繁琐。
 *
 * <p>
 * <b>主要功能：</b>
 * <ul>
 *     <li>单个对象转换：将源对象属性拷贝到目标对象</li>
 *     <li>集合转换：将源集合批量拷贝为目标类型集合</li>
 *     <li>属性忽略：支持排除不需要拷贝的属性</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 单个对象转换
 * UserDTO dto = ConvertUtils.sourceToTarget(user, UserDTO.class);
 *
 * // 集合批量转换
 * List<UserVO> voList = ConvertUtils.sourceToTarget(userList, UserVO.class);
 *
 * // 排除不需要拷贝的属性
 * UserDTO dto = ConvertUtils.sourceToTarget(user, UserDTO.class, "password", "secret");
 * }</pre>
 *
 * @author xiyao
 * @see BeanUtils
 */
@Slf4j
@UtilityClass
public class ConvertUtils {

    /**
     * 将源对象属性拷贝到目标对象
     * <p>
     * 通过反射创建目标对象实例，然后使用 Spring BeanUtils 拷贝属性。
     * 要求目标类有无参构造方法。
     *
     * @param source           源对象（不能为 null）
     * @param target           目标类型
     * @param ignoreProperties 拷贝时忽略的属性名（可选），如密码、密钥等敏感字段
     * @param <T>              目标类型
     * @return 目标类型实例，拷贝失败返回 null
     * @throws NullPointerException source 为 null 时
     */
    public <T> T sourceToTarget(Object source, Class<T> target, String... ignoreProperties) {
        // 空值校验
        if (ObjectUtil.isNull(source)) {
            return null;
        }

        T targetObject = null;
        try {
            // 通过反射创建目标对象实例（需要目标类有无参构造方法）
            targetObject = target.getDeclaredConstructor().newInstance();
            // 使用 Spring BeanUtils 拷贝属性，可选排除某些属性
            BeanUtils.copyProperties(source, targetObject, ignoreProperties);
        } catch (Exception e) {
            // 记录转换异常，包含源类型和目标类型信息便于排查
            log.error("对象转换出错: source={}, target={}", source.getClass().getName(), target.getName(), e);
        }

        return targetObject;
    }

    /**
     * 将源集合批量转换为目标类型集合
     * <p>
     * 使用并行流提升大数据量集合的转换性能。
     * 保持原集合的迭代顺序。
     *
     * @param sourceList 源集合（不能为 null）
     * @param target          目标类型
     * @param ignoreProperties 拷贝时忽略的属性名（可选）
     * @param <T>            目标类型
     * @return 目标类型集合，源集合为空时返回空列表，转换失败返回空列表
     */
    public <T> List<T> sourceToTarget(Collection<?> sourceList, Class<T> target, String... ignoreProperties) {
        // 空值校验
        if (ObjectUtil.isNull(sourceList)) {
            return null;
        }

        // 创建结果集合，预分配容量避免扩容开销
        List<T> targetList = new ArrayList<>(sourceList.size());

        // 使用并行流提升性能，forEachOrdered 保持顺序
        sourceList.parallelStream().forEachOrdered(source -> {
            try {
                // 为每个源对象创建目标类型实例
                T targetObject = target.getDeclaredConstructor().newInstance();
                // 拷贝属性
                BeanUtils.copyProperties(source, targetObject, ignoreProperties);
                targetList.add(targetObject);
            } catch (Exception e) {
                // 记录单个元素转换异常，继续处理其他元素
                log.error("集合元素转换出错: source={}, target={}", source.getClass().getName(), target.getName(), e);
            }
        });

        return targetList;
    }
}