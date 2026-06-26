package com.xiyao.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.service.dto.SleepCheckinDto;
import com.xiyao.service.dto.WorkoutCheckinDto;
import com.xiyao.service.entity.CheckinRecord;
import com.xiyao.service.mapper.CheckinRecordMapper;
import com.xiyao.service.service.CheckinRecordService;
import com.xiyao.service.vo.CheckinRecordVo;
import com.xiyao.service.vo.CheckinStatsVo;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 打卡记录 服务实现类
 * </p>
 *
 * @author xiyao
 */
@Service
public class CheckinRecordServiceImpl extends MyBaseServiceImpl<CheckinRecordMapper, CheckinRecord>
        implements CheckinRecordService {

    private static final int CHECKIN_TYPE_SLEEP = 1;
    private static final int CHECKIN_TYPE_WORKOUT = 2;

    // ==================== 睡觉打卡 ====================

    @Override
    public void createSleep(Long userId, SleepCheckinDto dto) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_SLEEP, LocalDate.now());
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("今日睡觉打卡已存在，请使用更新接口");
        }

        CheckinRecord record = new CheckinRecord()
                .setUserId(userId)
                .setCheckinType(CHECKIN_TYPE_SLEEP)
                .setRecordDate(LocalDate.now())
                .setBedTime(dto.getBedTime())
                .setRemark(dto.getRemark());

        this.save(record);
    }

    @Override
    public void updateSleep(Long userId, SleepCheckinDto dto) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_SLEEP, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日睡觉打卡不存在，请使用新增接口");
        }

        exist.setBedTime(dto.getBedTime());
        exist.setRemark(dto.getRemark());
        this.updateById(exist);
    }

    @Override
    public void deleteSleep(Long userId) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_SLEEP, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日睡觉打卡不存在");
        }
        this.removeById(exist.getId());
    }

    @Override
    public CheckinRecordVo getTodaySleep(Long userId) {
        CheckinRecord record = getByUserAndDate(userId, CHECKIN_TYPE_SLEEP, LocalDate.now());
        return convertToVo(record);
    }

    @Override
    public CheckinStatsVo getSleepStats(Long userId) {
        return buildStats(userId, CHECKIN_TYPE_SLEEP);
    }

    // ==================== 健身打卡 ====================

    @Override
    public void createWorkout(Long userId, WorkoutCheckinDto dto) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_WORKOUT, LocalDate.now());
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("今日健身打卡已存在，请使用更新接口");
        }

        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();
        int duration = (int) java.time.Duration.between(startTime, endTime).toMinutes();

        CheckinRecord record = new CheckinRecord()
                .setUserId(userId)
                .setCheckinType(CHECKIN_TYPE_WORKOUT)
                .setRecordDate(LocalDate.now())
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setDuration(duration)
                .setExerciseType(dto.getExerciseType())
                .setRemark(dto.getRemark());

        this.save(record);
    }

    @Override
    public void updateWorkout(Long userId, WorkoutCheckinDto dto) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_WORKOUT, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日健身打卡不存在，请使用新增接口");
        }

        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();
        int duration = (int) java.time.Duration.between(startTime, endTime).toMinutes();

        exist.setStartTime(startTime);
        exist.setEndTime(endTime);
        exist.setDuration(duration);
        exist.setExerciseType(dto.getExerciseType());
        exist.setRemark(dto.getRemark());
        this.updateById(exist);
    }

    @Override
    public void deleteWorkout(Long userId) {
        CheckinRecord exist = getByUserAndDate(userId, CHECKIN_TYPE_WORKOUT, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日健身打卡不存在");
        }
        this.removeById(exist.getId());
    }

    @Override
    public CheckinRecordVo getTodayWorkout(Long userId) {
        CheckinRecord record = getByUserAndDate(userId, CHECKIN_TYPE_WORKOUT, LocalDate.now());
        return convertToVo(record);
    }

    @Override
    public CheckinStatsVo getWorkoutStats(Long userId) {
        return buildStats(userId, CHECKIN_TYPE_WORKOUT);
    }

    // ==================== 私有方法 ====================

    private CheckinRecord getByUserAndDate(Long userId, Integer checkinType, LocalDate recordDate) {
        return getOne(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinType, checkinType)
                .eq(CheckinRecord::getRecordDate, recordDate));
    }

    private CheckinRecordVo convertToVo(CheckinRecord record) {
        if (record == null) {
            return null;
        }
        return new CheckinRecordVo()
                .setId(record.getId())
                .setCheckinType(record.getCheckinType())
                .setRecordDate(record.getRecordDate())
                .setBedTime(record.getBedTime())
                .setStartTime(record.getStartTime())
                .setEndTime(record.getEndTime())
                .setDuration(record.getDuration())
                .setExerciseType(record.getExerciseType())
                .setRemark(record.getRemark());
    }

    private CheckinStatsVo buildStats(Long userId, Integer checkinType) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());

        List<CheckinRecord> recentRecords = getRecentRecords(userId, checkinType, 7);
        int continuousDays = countContinuousDays(userId, checkinType);
        int thisWeek = countByDateRange(userId, checkinType, weekStart, today);
        int thisMonth = countByDateRange(userId, checkinType, monthStart, today);

        List<CheckinStatsVo.DayStatus> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            final LocalDate finalDate = date;
            CheckinRecord record = recentRecords.stream()
                    .filter(r -> r.getRecordDate().equals(finalDate))
                    .findFirst()
                    .orElse(null);

            CheckinStatsVo.DayStatus status = new CheckinStatsVo.DayStatus()
                    .setDate(date)
                    .setDone(record != null);

            if (record != null) {
                if (checkinType == CHECKIN_TYPE_SLEEP) {
                    status.setBedTime(record.getBedTime() != null ? record.getBedTime().toString() : null);
                } else {
                    status.setExerciseType(record.getExerciseType());
                }
            }
            last7Days.add(status);
        }

        return new CheckinStatsVo()
                .setContinuousDays(continuousDays)
                .setTotalDays(continuousDays)
                .setThisWeek(thisWeek)
                .setThisMonth(thisMonth)
                .setLast7Days(last7Days);
    }

    private List<CheckinRecord> getRecentRecords(Long userId, Integer checkinType, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return list(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinType, checkinType)
                .ge(CheckinRecord::getRecordDate, startDate)
                .orderByAsc(CheckinRecord::getRecordDate));
    }

    private int countContinuousDays(Long userId, Integer checkinType) {
        int continuousDays = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            CheckinRecord record = getByUserAndDate(userId, checkinType, date);
            if (ObjectUtil.isNotNull(record)) {
                continuousDays++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return continuousDays;
    }

    private int countByDateRange(Long userId, Integer checkinType, LocalDate startDate, LocalDate endDate) {
        Long count = count(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinType, checkinType)
                .ge(CheckinRecord::getRecordDate, startDate)
                .le(CheckinRecord::getRecordDate, endDate));
        return count.intValue();
    }
}
