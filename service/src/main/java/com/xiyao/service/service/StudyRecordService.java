package com.xiyao.service.service;

import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.service.dto.StudyDto;
import com.xiyao.service.entity.StudyRecord;
import com.xiyao.service.vo.PageResultVo;
import com.xiyao.service.vo.StudyRecordVo;
import com.xiyao.service.vo.StudyStatsVo;

/**
 * <p>
 * 学习记录 服务类
 * </p>
 *
 * @author xiyao
 */
public interface StudyRecordService extends MyBaseService<StudyRecord> {

    /**
     * 新增学习记录
     *
     * @param userId 用户ID
     * @param dto 学习请求
     */
    void create(Long userId, StudyDto dto);

    /**
     * 更新学习记录
     *
     * @param userId 用户ID
     * @param dto 学习请求
     */
    void update(Long userId, StudyDto dto);

    /**
     * 删除学习记录
     *
     * @param userId 用户ID
     */
    void delete(Long userId);

    /**
     * 获取今日学习记录
     *
     * @param userId 用户ID
     * @return 学习记录VO
     */
    StudyRecordVo getToday(Long userId);

    /**
     * 分页查询学习记录
     *
     * @param userId 用户ID
     * @param subject 学习主题（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResultVo<StudyRecordVo> listByPage(Long userId, String subject, int page, int pageSize);

    /**
     * 获取学习统计
     *
     * @param userId 用户ID
     * @return 统计VO
     */
    StudyStatsVo getStats(Long userId);
}
