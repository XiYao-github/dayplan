package com.xiyao.service.service;

import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.service.dto.SleepCheckinDto;
import com.xiyao.service.dto.WorkoutCheckinDto;
import com.xiyao.service.entity.CheckinRecord;
import com.xiyao.service.vo.CheckinRecordVo;
import com.xiyao.service.vo.CheckinStatsVo;

/**
 * <p>
 * 打卡记录 服务类
 * </p>
 *
 * @author xiyao
 */
public interface CheckinRecordService extends MyBaseService<CheckinRecord> {

    /**
     * 新增睡觉打卡
     *
     * @param userId 用户ID
     * @param dto 睡觉打卡请求
     */
    void createSleep(Long userId, SleepCheckinDto dto);

    /**
     * 更新睡觉打卡
     *
     * @param userId 用户ID
     * @param dto 睡觉打卡请求
     */
    void updateSleep(Long userId, SleepCheckinDto dto);

    /**
     * 删除睡觉打卡
     *
     * @param userId 用户ID
     */
    void deleteSleep(Long userId);

    /**
     * 获取今日睡觉打卡
     *
     * @param userId 用户ID
     * @return 睡觉打卡VO
     */
    CheckinRecordVo getTodaySleep(Long userId);

    /**
     * 获取睡觉打卡统计
     *
     * @param userId 用户ID
     * @return 打卡统计VO
     */
    CheckinStatsVo getSleepStats(Long userId);

    /**
     * 新增健身打卡
     *
     * @param userId 用户ID
     * @param dto 健身打卡请求
     */
    void createWorkout(Long userId, WorkoutCheckinDto dto);

    /**
     * 更新健身打卡
     *
     * @param userId 用户ID
     * @param dto 健身打卡请求
     */
    void updateWorkout(Long userId, WorkoutCheckinDto dto);

    /**
     * 删除健身打卡
     *
     * @param userId 用户ID
     */
    void deleteWorkout(Long userId);

    /**
     * 获取今日健身打卡
     *
     * @param userId 用户ID
     * @return 健身打卡VO
     */
    CheckinRecordVo getTodayWorkout(Long userId);

    /**
     * 获取健身打卡统计
     *
     * @param userId 用户ID
     * @return 打卡统计VO
     */
    CheckinStatsVo getWorkoutStats(Long userId);
}
