package com.xiyao.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import com.xiyao.framework.exception.BusinessException;
import com.xiyao.service.dto.WritingDto;
import com.xiyao.service.entity.WritingRecord;
import com.xiyao.service.mapper.WritingRecordMapper;
import com.xiyao.service.service.WritingRecordService;
import com.xiyao.service.vo.WritingRecordVo;
import com.xiyao.service.vo.WritingStatsVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * 写作记录 服务实现类
 * </p>
 *
 * @author xiyao
 */
@Service
public class WritingRecordServiceImpl extends MyBaseServiceImpl<WritingRecordMapper, WritingRecord>
        implements WritingRecordService {

    @Override
    public void create(Long userId, WritingDto dto) {
        WritingRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNotNull(exist)) {
            throw new BusinessException("今日写作记录已存在，请使用更新接口");
        }

        WritingRecord record = new WritingRecord()
                .setUserId(userId)
                .setRecordDate(LocalDate.now())
                .setTitle(dto.getTitle())
                .setWordCount(dto.getWordCount())
                .setSourceText(dto.getSourceText())
                .setTranslatedText(dto.getTranslatedText())
                .setImageUrls(dto.getImageUrls())
                .setRemark(dto.getRemark());

        this.save(record);
    }

    @Override
    public void update(Long userId, WritingDto dto) {
        WritingRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日写作记录不存在，请使用新增接口");
        }

        exist.setTitle(dto.getTitle());
        exist.setWordCount(dto.getWordCount());
        exist.setSourceText(dto.getSourceText());
        exist.setTranslatedText(dto.getTranslatedText());
        exist.setImageUrls(dto.getImageUrls());
        exist.setRemark(dto.getRemark());
        this.updateById(exist);
    }

    @Override
    public void delete(Long userId) {
        WritingRecord exist = getByUserAndDate(userId, LocalDate.now());
        if (ObjectUtil.isNull(exist)) {
            throw new BusinessException("今日写作记录不存在");
        }
        this.removeById(exist.getId());
    }

    @Override
    public WritingRecordVo getToday(Long userId) {
        WritingRecord record = getByUserAndDate(userId, LocalDate.now());
        return convertToVo(record);
    }

    @Override
    public WritingStatsVo getStats(Long userId) {
        int continuousDays = countContinuousDays(userId);

        return new WritingStatsVo()
                .setContinuousDays(continuousDays)
                .setTotalDays(continuousDays);
    }

    private WritingRecord getByUserAndDate(Long userId, LocalDate recordDate) {
        return getOne(new LambdaQueryWrapper<WritingRecord>()
                .eq(WritingRecord::getUserId, userId)
                .eq(WritingRecord::getRecordDate, recordDate));
    }

    private int countContinuousDays(Long userId) {
        int continuousDays = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            WritingRecord record = getByUserAndDate(userId, date);
            if (ObjectUtil.isNotNull(record)) {
                continuousDays++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return continuousDays;
    }

    private WritingRecordVo convertToVo(WritingRecord record) {
        if (record == null) {
            return null;
        }

        WritingRecordVo vo = new WritingRecordVo()
                .setId(record.getId())
                .setRecordDate(record.getRecordDate())
                .setTitle(record.getTitle())
                .setWordCount(record.getWordCount())
                .setSourceText(record.getSourceText())
                .setTranslatedText(record.getTranslatedText())
                .setRemark(record.getRemark());

        if (StringUtils.hasText(record.getImageUrls())) {
            vo.setImageUrls(Arrays.asList(record.getImageUrls().split(",")));
        } else {
            vo.setImageUrls(Collections.emptyList());
        }

        return vo;
    }
}
