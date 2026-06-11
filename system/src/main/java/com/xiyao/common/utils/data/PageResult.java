package com.xiyao.common.utils.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装类
 * <p>
 * 用于封装 MyBatis-Plus 分页查询结果，作为 Result 的 data 字段使用。
 *
 * <p>
 * <b>字段说明：</b>
 * <ul>
 *     <li>records：当前页的数据记录列表</li>
 *     <li>total：符合查询条件的总记录数</li>
 *     <li>size：每页显示条数</li>
 *     <li>current：当前页码</li>
 *     <li>pages：总页数</li>
 * </ul>
 *
 * <p>
 * <b>典型用法：</b>
 * <pre>{@code
 * // Service 层构建分页结果
 * IPage<User> userPage = userMapper.selectPage(page, wrapper);
 * List<UserVO> voList = userPage.getRecords().stream()
 *         .map(this::convertToVO)
 *         .collect(Collectors.toList());
 * PageResult<UserVO> result = PageResult.page(userPage, voList);
 *
 * // Controller 层返回
 * return Result.ok(result);
 * }</pre>
 *
 * @param <T> 分页数据的类型，通常为 VO 对象
 * @author xiyao
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页的数据记录列表
     * <p>
     * 元素类型为 VO 对象，禁止直接存放 entity
     */
    private List<T> records;

    /**
     * 符合查询条件的总记录数
     * <p>
     * 用于前端计算总页数和显示总数
     */
    private long total;

    /**
     * 每页显示条数
     * <p>
     * 即前端传入的 pageSize
     */
    private long size;

    /**
     * 当前页码
     * <p>
     * 即前端传入的 current
     */
    private long current;

    /**
     * 总页数
     * <p>
     * 由 total 和 size 计算得出
     */
    private long pages;

    /**
     * 构建空的分页结果
     * <p>
     * 用于查询结果为空时返回空的分页响应。
     *
     * @param <T> 数据类型
     * @return 空的分页结果，records 为空列表，各项数值均为 0
     */
    public static <T> PageResult<T> empty() {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);
        pageResult.setSize(0);
        pageResult.setCurrent(0);
        pageResult.setPages(0);
        return pageResult;
    }

    /**
     * 从 MyBatis-Plus 的 IPage 对象构建 PageResult
     * <p>
     * 此方法为便捷工具，自动提取 IPage 中的各字段并构建 PageResult。
     * 传入的 records 需要提前转换为 VO 对象。
     *
     * @param page   MyBatis-Plus 分页查询结果
     * @param voList 转换后的 VO 对象列表
     * @param <T>    数据类型
     * @return PageResult 实例
     */
    public static <T> PageResult<T> page(IPage<?> page, List<T> voList) {
        // 参数校验：page 为 null 时返回空结果
        if (ObjectUtil.isNull(page)) {
            return empty();
        }
        // 参数校验：voList 为空时返回空结果
        if (CollUtil.isEmpty(voList)) {
            return empty();
        }

        // 创建 PageResult 并设置各字段
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setPages(page.getPages());

        return pageResult;
    }

}