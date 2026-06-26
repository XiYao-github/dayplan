package com.xiyao.service.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.service.dto.PlanDto;
import com.xiyao.service.dto.RecordDto;
import com.xiyao.service.dto.SummaryDto;
import com.xiyao.service.service.DailyRecordService;
import com.xiyao.service.vo.DailyRecordListVo;
import com.xiyao.service.vo.DailyRecordVo;
import com.xiyao.service.vo.DailyStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 每日记录接口
 * </p>
 *
 * @author xiyao
 */
@RestController
@RequestMapping("/app/daily")
@RequiredArgsConstructor
public class DailyRecordController extends MyBaseController {

    private final DailyRecordService dailyRecordService;

    // ==================== 计划 ====================

    /**
     * 新增计划
     */
    @PostMapping("/plan")
    public void createPlan(
            @RequestParam Long userId,
            @RequestBody PlanDto dto
    ) {
        dailyRecordService.createPlan(userId, dto);
    }

    /**
     * 更新计划
     */
    @PutMapping("/plan")
    public void updatePlan(
            @RequestParam Long userId,
            @RequestBody PlanDto dto
    ) {
        dailyRecordService.updatePlan(userId, dto);
    }

    /**
     * 获取计划
     *
     * @param userId 用户ID
     * @param period 周期维度(1.日 2.周 3.月 4.年)
     * @return 计划VO
     */
    @GetMapping("/plan")
    public DailyRecordVo getPlan(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer period
    ) {
        return dailyRecordService.getPlan(userId, period);
    }

    // ==================== 记录 ====================

    /**
     * 新增记录
     */
    @PostMapping("/record")
    public void createRecord(
            @RequestParam Long userId,
            @RequestBody RecordDto dto
    ) {
        dailyRecordService.createRecord(userId, dto);
    }

    /**
     * 更新记录
     */
    @PutMapping("/record")
    public void updateRecord(
            @RequestParam Long userId,
            @RequestBody RecordDto dto
    ) {
        dailyRecordService.updateRecord(userId, dto);
    }

    /**
     * 获取记录列表
     *
     * @param userId 用户ID
     * @param period 周期维度(1.日 2.周 3.月 4.年)
     * @return 记录列表
     */
    @GetMapping("/record")
    public List<DailyRecordVo> listRecord(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer period
    ) {
        return dailyRecordService.listRecord(userId, period);
    }

    // ==================== 总结 ====================

    /**
     * 新增总结
     */
    @PostMapping("/summary")
    public void createSummary(
            @RequestParam Long userId,
            @RequestBody SummaryDto dto
    ) {
        dailyRecordService.createSummary(userId, dto);
    }

    /**
     * 更新总结
     */
    @PutMapping("/summary")
    public void updateSummary(
            @RequestParam Long userId,
            @RequestBody SummaryDto dto
    ) {
        dailyRecordService.updateSummary(userId, dto);
    }

    /**
     * 获取总结
     *
     * @param userId 用户ID
     * @param period 周期维度(1.日 2.周 3.月 4.年)
     * @return 总结VO
     */
    @GetMapping("/summary")
    public DailyRecordVo getSummary(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer period
    ) {
        return dailyRecordService.getSummary(userId, period);
    }

    // ==================== 组合接口 ====================

    /**
     * 获取今日三记
     */
    @GetMapping("/today")
    public DailyRecordListVo getTodayAll(@RequestParam Long userId) {
        return dailyRecordService.getTodayAll(userId);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public DailyStatsVo getStats(@RequestParam Long userId) {
        return dailyRecordService.getStats(userId);
    }
}
