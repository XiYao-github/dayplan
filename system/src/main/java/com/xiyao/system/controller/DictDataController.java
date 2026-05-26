package com.xiyao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.entity.DictData;
import com.xiyao.system.service.IDictDataService;
import com.xiyao.system.vo.DictDataVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dict/data")
public class DictDataController extends MyBaseController {

    private final IDictDataService dictDataService;

    /**
     * 分页查询字典数据
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<Page<DictDataVo>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) Integer status) {

        Page<DictDataVo> page = new Page<>(pageNum, pageSize);
        Page<DictDataVo> result = dictDataService.pageDictData(page, dictType, dictLabel, status);
        return Result.ok(result);
    }

    /**
     * 根据字典类型查询字典数据（下拉框用）
     */
    @GetMapping("/options/{dictType}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<List<DictDataVo>> options(@PathVariable String dictType) {
        List<DictDataVo> list = dictDataService.listByDictType(dictType);
        return Result.ok(list);
    }

    /**
     * 获取字典数据详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<DictDataVo> getById(@PathVariable Long id) {
        DictDataVo vo = dictDataService.getDictDataById(id);
        return Result.ok(vo);
    }

    /**
     * 创建字典数据
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody DictData dictData) {
        dictDataService.createDictData(dictData);
        return Result.ok();
    }

    /**
     * 更新字典数据
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody DictData dictData) {
        dictDataService.updateDictData(dictData);
        return Result.ok();
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        dictDataService.deleteDictData(id);
        return Result.ok();
    }

    /**
     * 刷新字典缓存
     */
    @PostMapping("/refresh")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result<Void> refreshCache() {
        dictDataService.refreshCache();
        return Result.ok();
    }
}
