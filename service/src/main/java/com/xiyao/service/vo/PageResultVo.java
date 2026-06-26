package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 分页结果 VO
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class PageResultVo<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页
     */
    private Long page;

    /**
     * 每页数量
     */
    private Long pageSize;

    /**
     * 记录列表
     */
    private List<T> records;
}
