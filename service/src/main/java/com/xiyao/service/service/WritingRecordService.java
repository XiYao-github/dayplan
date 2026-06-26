package com.xiyao.service.service;

import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.service.dto.WritingDto;
import com.xiyao.service.entity.WritingRecord;
import com.xiyao.service.vo.WritingRecordVo;
import com.xiyao.service.vo.WritingStatsVo;

/**
 * <p>
 * 写作记录 服务类
 * </p>
 *
 * @author xiyao
 */
public interface WritingRecordService extends MyBaseService<WritingRecord> {

    /**
     * 新增写作记录
     *
     * @param userId 用户ID
     * @param dto 写作请求
     */
    void create(Long userId, WritingDto dto);

    /**
     * 更新写作记录
     *
     * @param userId 用户ID
     * @param dto 写作请求
     */
    void update(Long userId, WritingDto dto);

    /**
     * 删除写作记录
     *
     * @param userId 用户ID
     */
    void delete(Long userId);

    /**
     * 获取今日写作记录
     *
     * @param userId 用户ID
     * @return 写作记录VO
     */
    WritingRecordVo getToday(Long userId);

    /**
     * 获取写作统计
     *
     * @param userId 用户ID
     * @return 统计VO
     */
    WritingStatsVo getStats(Long userId);
}
