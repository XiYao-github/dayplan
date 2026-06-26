package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 总结请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class SummaryDto {

    /**
     * 周期维度(1.日 2.周 3.月 4.年)
     */
    private Integer period;

    /**
     * 总结内容
     */
    private String content;

    /**
     * 高光/成就
     */
    private String highlight;

    /**
     * 卡点/困难
     */
    private String blocker;
}
