package com.xiyao.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.service.dto.PlanDto;
import com.xiyao.service.dto.RecordDto;
import com.xiyao.service.dto.SummaryDto;
import com.xiyao.service.entity.DailyRecord;
import com.xiyao.service.mapper.DailyRecordMapper;
import com.xiyao.service.service.DailyRecordService;
import com.xiyao.service.utils.PeriodUtils;
import com.xiyao.service.vo.DailyRecordListVo;
import com.xiyao.service.vo.DailyRecordVo;
import com.xiyao.service.vo.DailyStatsVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 每日记录 服务实现类
 * </p>
 *
 * @author xiyao
 */
@Service
public class DailyRecordServiceImpl extends MyBaseServiceImpl<DailyRecordMapper, DailyRecord>
        implements DailyRecordService {

    private static final int RECORD_TYPE_PLAN = 1;
    private static final int RECORD_TYPE_RECORD = 2;
    private static final int RECORD_TYPE_SUMMARY = 3;

    // ==================== 计划 ====================

    @Override
    public void createPlan(Long userId, PlanDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_PLAN, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("当前周期计划已存在，请使用更新接口");
        }

        DailyRecord record = new DailyRecord()
                .setUserId(userId)
                .setRecordType(RECORD_TYPE_PLAN)
                .setPeriod(dto.getPeriod())
                .setPeriodValue(periodValue)
                .setContent(dto.getContent())
                .setCategory(dto.getCategory());

        this.save(record);
    }

    @Override
    public void updatePlan(Long userId, PlanDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_PLAN, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("当前周期计划不存在，请使用新增接口");
        }

        exist.setContent(dto.getContent());
        exist.setCategory(dto.getCategory());
        this.updateById(exist);
    }

    @Override
    public DailyRecordVo getPlan(Long userId, Integer period) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(period);
        DailyRecord record = getByUserAndPeriod(userId, RECORD_TYPE_PLAN, period, periodValue);
        return convertToVo(record);
    }

    // ==================== 记录 ====================

    @Override
    public void createRecord(Long userId, RecordDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_RECORD, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("当前周期记录已存在，请使用更新接口");
        }

        DailyRecord record = new DailyRecord()
                .setUserId(userId)
                .setRecordType(RECORD_TYPE_RECORD)
                .setPeriod(dto.getPeriod())
                .setPeriodValue(periodValue)
                .setContent(dto.getContent())
                .setCategory(dto.getCategory());

        this.save(record);
    }

    @Override
    public void updateRecord(Long userId, RecordDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_RECORD, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("当前周期记录不存在，请使用新增接口");
        }

        exist.setContent(dto.getContent());
        exist.setCategory(dto.getCategory());
        this.updateById(exist);
    }

    @Override
    public List<DailyRecordVo> listRecord(Long userId, Integer period) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(period);
        List<DailyRecord> records = listByUserAndPeriod(userId, period, periodValue);
        return records.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    // ==================== 总结 ====================

    @Override
    public void createSummary(Long userId, SummaryDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_SUMMARY, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("当前周期总结已存在，请使用更新接口");
        }

        DailyRecord record = new DailyRecord()
                .setUserId(userId)
                .setRecordType(RECORD_TYPE_SUMMARY)
                .setPeriod(dto.getPeriod())
                .setPeriodValue(periodValue)
                .setContent(dto.getContent())
                .setHighlight(dto.getHighlight())
                .setBlocker(dto.getBlocker());

        this.save(record);
    }

    @Override
    public void updateSummary(Long userId, SummaryDto dto) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(dto.getPeriod());
        DailyRecord exist = getByUserAndPeriod(userId, RECORD_TYPE_SUMMARY, dto.getPeriod(), periodValue);
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("当前周期总结不存在，请使用新增接口");
        }

        exist.setContent(dto.getContent());
        exist.setHighlight(dto.getHighlight());
        exist.setBlocker(dto.getBlocker());
        this.updateById(exist);
    }

    @Override
    public DailyRecordVo getSummary(Long userId, Integer period) {
        String periodValue = PeriodUtils.getCurrentPeriodValue(period);
        DailyRecord record = getByUserAndPeriod(userId, RECORD_TYPE_SUMMARY, period, periodValue);
        return convertToVo(record);
    }

    // ==================== 组合接口 ====================

    @Override
    public DailyRecordListVo getTodayAll(Long userId) {
        List<DailyRecord> records = listByUserAndPeriod(userId, PeriodUtils.PERIOD_DAY, PeriodUtils.today());

        DailyRecordListVo vo = new DailyRecordListVo();

        for (DailyRecord record : records) {
            if (record.getRecordType() == RECORD_TYPE_PLAN) {
                vo.setPlan(convertToVo(record));
            } else if (record.getRecordType() == RECORD_TYPE_RECORD) {
                if (vo.getRecords() == null) {
                    vo.setRecords(new ArrayList<>());
                }
                vo.getRecords().add(convertToVo(record));
            } else if (record.getRecordType() == RECORD_TYPE_SUMMARY) {
                vo.setSummary(convertToVo(record));
            }
        }

        return vo;
    }

    @Override
    public DailyStatsVo getStats(Long userId) {
        DailyStatsVo vo = new DailyStatsVo();

        // 今日状态
        DailyStatsVo.PeriodStatus todayStatus = new DailyStatsVo.PeriodStatus();
        todayStatus.setPlan(countByPeriod(userId, RECORD_TYPE_PLAN, PeriodUtils.PERIOD_DAY, PeriodUtils.today()) > 0);
        todayStatus.setRecord(countByPeriod(userId, RECORD_TYPE_RECORD, PeriodUtils.PERIOD_DAY, PeriodUtils.today()) > 0);
        todayStatus.setSummary(countByPeriod(userId, RECORD_TYPE_SUMMARY, PeriodUtils.PERIOD_DAY, PeriodUtils.today()) > 0);
        vo.setToday(todayStatus);

        // 本周状态
        DailyStatsVo.PeriodStatus weekStatus = new DailyStatsVo.PeriodStatus();
        weekStatus.setPlan((int) countByPeriod(userId, RECORD_TYPE_PLAN, PeriodUtils.PERIOD_WEEK, PeriodUtils.thisWeek()));
        weekStatus.setRecord((int) countByPeriod(userId, RECORD_TYPE_RECORD, PeriodUtils.PERIOD_WEEK, PeriodUtils.thisWeek()));
        weekStatus.setSummary((int) countByPeriod(userId, RECORD_TYPE_SUMMARY, PeriodUtils.PERIOD_WEEK, PeriodUtils.thisWeek()));
        vo.setThisWeek(weekStatus);

        // 本月状态
        DailyStatsVo.PeriodStatus monthStatus = new DailyStatsVo.PeriodStatus();
        monthStatus.setPlan((int) countByPeriod(userId, RECORD_TYPE_PLAN, PeriodUtils.PERIOD_MONTH, PeriodUtils.thisMonth()));
        monthStatus.setRecord((int) countByPeriod(userId, RECORD_TYPE_RECORD, PeriodUtils.PERIOD_MONTH, PeriodUtils.thisMonth()));
        monthStatus.setSummary((int) countByPeriod(userId, RECORD_TYPE_SUMMARY, PeriodUtils.PERIOD_MONTH, PeriodUtils.thisMonth()));
        vo.setThisMonth(monthStatus);

        return vo;
    }

    // ==================== 私有方法 ====================

    private DailyRecord getByUserAndPeriod(Long userId, Integer recordType, Integer period, String periodValue) {
        return getOne(new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .eq(DailyRecord::getRecordType, recordType)
                .eq(DailyRecord::getPeriod, period)
                .eq(DailyRecord::getPeriodValue, periodValue));
    }

    private List<DailyRecord> listByUserAndPeriod(Long userId, Integer period, String periodValue) {
        return list(new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .eq(DailyRecord::getPeriod, period)
                .eq(DailyRecord::getPeriodValue, periodValue)
                .orderByAsc(DailyRecord::getRecordType));
    }

    private long countByPeriod(Long userId, Integer recordType, Integer period, String periodValue) {
        return count(new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .eq(DailyRecord::getRecordType, recordType)
                .eq(DailyRecord::getPeriod, period)
                .eq(DailyRecord::getPeriodValue, periodValue));
    }

    private DailyRecordVo convertToVo(DailyRecord record) {
        if (record == null) {
            return null;
        }
        return new DailyRecordVo()
                .setId(record.getId())
                .setRecordType(record.getRecordType())
                .setPeriod(record.getPeriod())
                .setPeriodValue(record.getPeriodValue())
                .setContent(record.getContent())
                .setHighlight(record.getHighlight())
                .setBlocker(record.getBlocker())
                .setCategory(record.getCategory());
    }
}
