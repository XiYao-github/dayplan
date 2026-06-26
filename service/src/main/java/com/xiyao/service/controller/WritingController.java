package com.xiyao.service.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.service.dto.WritingDto;
import com.xiyao.service.service.WritingRecordService;
import com.xiyao.service.vo.WritingRecordVo;
import com.xiyao.service.vo.WritingStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 写作接口
 * </p>
 *
 * @author xiyao
 */
@RestController
@RequestMapping("/app/writing")
@RequiredArgsConstructor
public class WritingController extends MyBaseController {

    private final WritingRecordService writingRecordService;

    /**
     * 新增写作记录
     */
    @PostMapping
    public void create(
            @RequestParam Long userId,
            @RequestBody WritingDto dto
    ) {
        writingRecordService.create(userId, dto);
    }

    /**
     * 更新写作记录
     */
    @PutMapping
    public void update(
            @RequestParam Long userId,
            @RequestBody WritingDto dto
    ) {
        writingRecordService.update(userId, dto);
    }

    /**
     * 删除写作记录
     */
    @DeleteMapping
    public void delete(@RequestParam Long userId) {
        writingRecordService.delete(userId);
    }

    /**
     * 获取今日写作记录
     */
    @GetMapping
    public WritingRecordVo getToday(@RequestParam Long userId) {
        return writingRecordService.getToday(userId);
    }

    /**
     * 获取写作统计
     */
    @GetMapping("/stats")
    public WritingStatsVo getStats(@RequestParam Long userId) {
        return writingRecordService.getStats(userId);
    }
}
