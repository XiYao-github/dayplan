package com.xiyao.service.service;

import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.service.dto.PlanDto;
import com.xiyao.service.dto.RecordDto;
import com.xiyao.service.dto.SummaryDto;
import com.xiyao.service.entity.DailyRecord;
import com.xiyao.service.vo.DailyRecordListVo;
import com.xiyao.service.vo.DailyRecordVo;
import com.xiyao.service.vo.DailyStatsVo;

import java.util.List;

/**
 * <p>
 * 每日记录 服务类
 * </p>
 *
 * @author xiyao
 */
public interface DailyRecordService extends MyBaseService<DailyRecord> {

    // ==================== 计划 ====================

    /**
     * 新增计划
     */
    void createPlan(Long userId, PlanDto dto);

    /**
     * 更新计划
     */
    void updatePlan(Long userId, PlanDto dto);

    /**
     * 获取计划
     *
     * @param userId 用户ID
     * @param period 周期维度
     * @return 计划VO
     */
    DailyRecordVo getPlan(Long userId, Integer period);

    // ==================== 记录 ====================

    /**
     * 新增记录
     */
    void createRecord(Long userId, RecordDto dto);

    /**
     * 更新记录
     */
    void updateRecord(Long userId, RecordDto dto);

    /**
     * 获取记录列表
     *
     * @param userId 用户ID
     * @param period 周期维度
     * @return 记录列表VO
     */
    List<DailyRecordVo> listRecord(Long userId, Integer period);

    // ==================== 总结 ====================

    /**
     * 新增总结
     */
    void createSummary(Long userId, SummaryDto dto);

    /**
     * 更新总结
     */
    void updateSummary(Long userId, SummaryDto dto);

    /**
     * 获取总结
     *
     * @param userId 用户ID
     * @param period 周期维度
     * @return 总结VO
     */
    DailyRecordVo getSummary(Long userId, Integer period);

    // ==================== 组合接口 ====================

    /**
     * 获取今日三记
     *
     * @param userId 用户ID
     * @return 今日三记VO
     */
    DailyRecordListVo getTodayAll(Long userId);

    /**
     * 获取统计数据
     *
     * @param userId 用户ID
     * @return 统计VO
     */
    DailyStatsVo getStats(Long userId);
}
