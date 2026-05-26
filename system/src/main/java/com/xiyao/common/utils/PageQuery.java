package com.xiyao.common.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询参数类
 * <p>
 * 用于接收前端传来的分页和排序参数。
 * 通常与 MyBatis-Plus 的 Page<T> 配合使用。
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * @GetMapping("/user/list")
 * public Result&lt;TableDataInfo&lt;User&gt;&gt; list(UserQuery query) {
 *     Page&lt;User&gt; page = query.build();
 *     IPage&lt;User&gt; result = userService.page(page);
 *     return Result.ok(TableDataInfo.build(result));
 * }
 * }</pre>
 *
 * @author xiyao
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页数（从 1 开始）
     */
    private Integer pageNum;

    /**
     * 排序列（字段名，多个用逗号分隔）
     */
    private String orderByColumn;

    /**
     * 排序方向（asc/desc，多个用逗号分隔）
     */
    private String isAsc;

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页显示记录数（默认查全部）
     */
    public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;

    /**
     * 计算当前记录起始索引
     * <p>
     * 计算公式：(pageNum - 1) * pageSize
     *
     * @return 起始索引，pageSize 为 null 时返回 null
     */
    @JsonIgnore
    public Integer getFirstNum() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 构造函数
     *
     * @param pageSize 每页大小
     * @param pageNum   当前页数
     */
    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}