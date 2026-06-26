package com.xiyao.service.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.service.dto.StudyDto;
import com.xiyao.service.service.StudyRecordService;
import com.xiyao.service.vo.PageResultVo;
import com.xiyao.service.vo.StudyRecordVo;
import com.xiyao.service.vo.StudyStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学习接口
 * </p>
 *
 * @author xiyao
 */
@RestController
@RequestMapping("/app/study")
@RequiredArgsConstructor
public class StudyController extends MyBaseController {

    private final StudyRecordService studyRecordService;

    /**
     * 新增学习记录
     */
    @PostMapping
    public void create(
            @RequestParam Long userId,
            @RequestBody StudyDto dto
    ) {
        studyRecordService.create(userId, dto);
    }

    /**
     * 更新学习记录
     */
    @PutMapping
    public void update(
            @RequestParam Long userId,
            @RequestBody StudyDto dto
    ) {
        studyRecordService.update(userId, dto);
    }

    /**
     * 删除学习记录
     */
    @DeleteMapping
    public void delete(@RequestParam Long userId) {
        studyRecordService.delete(userId);
    }

    /**
     * 获取今日学习记录
     */
    @GetMapping
    public StudyRecordVo getToday(@RequestParam Long userId) {
        return studyRecordService.getToday(userId);
    }

    /**
     * 分页查询学习记录
     */
    @GetMapping("/list")
    public PageResultVo<StudyRecordVo> listByPage(
            @RequestParam Long userId,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return studyRecordService.listByPage(userId, subject, page, pageSize);
    }

    /**
     * 获取学习统计
     */
    @GetMapping("/stats")
    public StudyStatsVo getStats(@RequestParam Long userId) {
        return studyRecordService.getStats(userId);
    }
}
