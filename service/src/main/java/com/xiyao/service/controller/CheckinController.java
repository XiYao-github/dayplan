package com.xiyao.service.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.service.dto.SleepCheckinDto;
import com.xiyao.service.dto.WorkoutCheckinDto;
import com.xiyao.service.service.CheckinRecordService;
import com.xiyao.service.vo.CheckinRecordVo;
import com.xiyao.service.vo.CheckinStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 打卡接口
 * </p>
 *
 * @author xiyao
 */
@RestController
@RequestMapping("/app/checkin")
@RequiredArgsConstructor
public class CheckinController extends MyBaseController {

    private final CheckinRecordService checkinRecordService;

    // ==================== 睡觉打卡 ====================

    /**
     * 新增睡觉打卡
     */
    @PostMapping("/sleep")
    public void createSleep(
            @RequestParam Long userId,
            @RequestBody SleepCheckinDto dto
    ) {
        checkinRecordService.createSleep(userId, dto);
    }

    /**
     * 更新睡觉打卡
     */
    @PutMapping("/sleep")
    public void updateSleep(
            @RequestParam Long userId,
            @RequestBody SleepCheckinDto dto
    ) {
        checkinRecordService.updateSleep(userId, dto);
    }

    /**
     * 删除睡觉打卡
     */
    @DeleteMapping("/sleep")
    public void deleteSleep(@RequestParam Long userId) {
        checkinRecordService.deleteSleep(userId);
    }

    /**
     * 获取今日睡觉打卡
     */
    @GetMapping("/sleep")
    public CheckinRecordVo getTodaySleep(@RequestParam Long userId) {
        return checkinRecordService.getTodaySleep(userId);
    }

    /**
     * 获取睡觉打卡统计
     */
    @GetMapping("/sleep/stats")
    public CheckinStatsVo getSleepStats(@RequestParam Long userId) {
        return checkinRecordService.getSleepStats(userId);
    }

    // ==================== 健身打卡 ====================

    /**
     * 新增健身打卡
     */
    @PostMapping("/workout")
    public void createWorkout(
            @RequestParam Long userId,
            @RequestBody WorkoutCheckinDto dto
    ) {
        checkinRecordService.createWorkout(userId, dto);
    }

    /**
     * 更新健身打卡
     */
    @PutMapping("/workout")
    public void updateWorkout(
            @RequestParam Long userId,
            @RequestBody WorkoutCheckinDto dto
    ) {
        checkinRecordService.updateWorkout(userId, dto);
    }

    /**
     * 删除健身打卡
     */
    @DeleteMapping("/workout")
    public void deleteWorkout(@RequestParam Long userId) {
        checkinRecordService.deleteWorkout(userId);
    }

    /**
     * 获取今日健身打卡
     */
    @GetMapping("/workout")
    public CheckinRecordVo getTodayWorkout(@RequestParam Long userId) {
        return checkinRecordService.getTodayWorkout(userId);
    }

    /**
     * 获取健身打卡统计
     */
    @GetMapping("/workout/stats")
    public CheckinStatsVo getWorkoutStats(@RequestParam Long userId) {
        return checkinRecordService.getWorkoutStats(userId);
    }
}
