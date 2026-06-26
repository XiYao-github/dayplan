package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 记录请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class RecordDto {

    /**
     * 周期维度(1.日 2.周 3.月 4.年)
     */
    private Integer period;

    /**
     * 记录内容
     */
    private String content;

    /**
     * 分类(learn/work/life)
     */
    private String category;
}
