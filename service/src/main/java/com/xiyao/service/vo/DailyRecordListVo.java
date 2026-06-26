package com.xiyao.service.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 每日三记 VO（包含计划、记录、总结）
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class DailyRecordListVo {

    /**
     * 计划
     */
    private DailyRecordVo plan;

    /**
     * 记录列表
     */
    private List<DailyRecordVo> records;

    /**
     * 总结
     */
    private DailyRecordVo summary;
}
