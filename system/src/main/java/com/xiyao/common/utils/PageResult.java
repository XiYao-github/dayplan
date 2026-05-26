package com.xiyao.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装类
 *
 * <p>用于封装 MyBatis-Plus 分页查询结果，作为 Result 的 data 字段使用。
 *
 * <p>典型用法：
 * <pre>{@code
 * // Service 层构建分页结果
 * IPage<User> userPage = userMapper.selectPage(page, wrapper);
 * List<UserVO> voList = userPage.getRecords().stream()
 *         .map(this::convertToVO)
 *         .collect(Collectors.toList());
 * PageResult<UserVO> result = new PageResult<>(voList, userPage.getTotal(),
 *         (int) userPage.getSize(), (int) userPage.getCurrent(),
 *         (int) userPage.getPages());
 *
 * // Controller 层返回
 * return Result.ok(result);
 * }</pre>
 *
 * @param <T> 分页数据的类型，通常为 VO 对象
 * @author xiyao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页的数据记录列表，元素类型为 VO 对象，禁止直接存放 entity
     */
    private List<T> records;

    /**
     * 符合查询条件的总记录数，用于前端计算总页数和显示总数
     */
    private long total;

    /**
     * 每页显示条数，即前端传入的 pageSize
     */
    private int size;

    /**
     * 当前页码，即前端传入的 current
     */
    private int current;

    /**
     * 总页数，由 total 和 size 计算得出
     */
    private int pages;

    /**
     * 构建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空的分页结果，records 为空列表，各项数值均为 0
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L, 0, 0, 0);
    }

    /**
     * 从 MyBatis-Plus 的 IPage 对象构建 PageResult
     *
     * <p>此方法为便捷工具，自动提取 IPage 中的各字段并构建 PageResult。
     * 传入的 records 需要提前转换为 VO 对象。
     *
     * @param page     MyBatis-Plus 分页查询结果
     * @param voList   转换后的 VO 对象列表
     * @param <T>      数据类型
     * @return PageResult 实例
     */
    public static <T> PageResult<T> from(com.baomidou.mybatisplus.core.metadata.IPage<?> page, List<T> voList) {
        return new PageResult<>(
                voList,
                page.getTotal(),
                (int) page.getSize(),
                (int) page.getCurrent(),
                (int) page.getPages()
        );
    }
}