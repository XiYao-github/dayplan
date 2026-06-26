package com.xiyao.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.service.dto.StudyDto;
import com.xiyao.service.entity.StudyRecord;
import com.xiyao.service.mapper.StudyRecordMapper;
import com.xiyao.service.service.StudyRecordService;
import com.xiyao.service.vo.PageResultVo;
import com.xiyao.service.vo.StudyRecordVo;
import com.xiyao.service.vo.StudyStatsVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习记录 服务实现类
 * </p>
 *
 * @author xiyao
 */
@Service
public class StudyRecordServiceImpl extends MyBaseServiceImpl<StudyRecordMapper, StudyRecord>
        implements StudyRecordService {

    @Override
    public void create(Long userId, StudyDto dto) {
        StudyRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("今日学习记录已存在，请使用更新接口");
        }

        StudyRecord record = new StudyRecord()
                .setUserId(userId)
                .setRecordDate(LocalDate.now())
                .setSubject(dto.getSubject())
                .setTopic(dto.getTopic())
                .setDuration(dto.getDuration())
                .setImageUrls(dto.getImageUrls())
                .setRemark(dto.getRemark());

        this.save(record);
    }

    @Override
    public void update(Long userId, StudyDto dto) {
        StudyRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日学习记录不存在，请使用新增接口");
        }

        exist.setSubject(dto.getSubject());
        exist.setTopic(dto.getTopic());
        exist.setDuration(dto.getDuration());
        exist.setImageUrls(dto.getImageUrls());
        exist.setRemark(dto.getRemark());
        this.updateById(exist);
    }

    @Override
    public void delete(Long userId) {
        StudyRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日学习记录不存在");
        }
        this.removeById(exist.getId());
    }

    @Override
    public StudyRecordVo getToday(Long userId) {
        StudyRecord record = getByUserAndDate(userId, LocalDate.now());
        return convertToVo(record);
    }

    @Override
    public PageResultVo<StudyRecordVo> listByPage(Long userId, String subject, int page, int pageSize) {
        Page<StudyRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .eq(!StringUtils.isEmpty(subject), StudyRecord::getSubject, subject)
                .orderByDesc(StudyRecord::getRecordDate);

        Page<StudyRecord> result = page(pageParam, wrapper);

        PageResultVo<StudyRecordVo> vo = new PageResultVo<>();
        vo.setPage((long) page);
        vo.setPageSize((long) pageSize);
        vo.setTotal(result.getTotal());
        vo.setRecords(result.getRecords().stream().map(this::convertToVo).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public StudyStatsVo getStats(Long userId) {
        int continuousDays = countContinuousDays(userId);
        int thisWeekDuration = sumThisWeekDuration(userId);

        return new StudyStatsVo()
                .setContinuousDays(continuousDays)
                .setThisWeekDuration(thisWeekDuration)
                .setTotalDays(continuousDays);
    }

    private StudyRecord getByUserAndDate(Long userId, LocalDate recordDate) {
        return getOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getRecordDate, recordDate));
    }

    private int sumThisWeekDuration(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<StudyRecord> records = list(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .ge(StudyRecord::getRecordDate, weekStart)
                .le(StudyRecord::getRecordDate, today));

        return records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();
    }

    private int countContinuousDays(Long userId) {
        int continuousDays = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            StudyRecord record = getByUserAndDate(userId, date);
            if (ObjectUtil.isNotNull(record)) {
                continuousDays++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return continuousDays;
    }

    private StudyRecordVo convertToVo(StudyRecord record) {
        if (record == null) {
            return null;
        }

        StudyRecordVo vo = new StudyRecordVo()
                .setId(record.getId())
                .setRecordDate(record.getRecordDate())
                .setSubject(record.getSubject())
                .setTopic(record.getTopic())
                .setDuration(record.getDuration())
                .setRemark(record.getRemark());

        if (StringUtils.hasText(record.getImageUrls())) {
            vo.setImageUrls(Arrays.asList(record.getImageUrls().split(",")));
        } else {
            vo.setImageUrls(Collections.emptyList());
        }

        return vo;
    }
}
