package com.xiyao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.entity.DictType;
import com.xiyao.system.service.IDictTypeService;
import com.xiyao.system.vo.DictTypeVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型控制器
 *
 * @author xiyao
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dict/type")
public class DictTypeController extends MyBaseController {

    private final IDictTypeService dictTypeService;

    /**
     * 分页查询字典类型
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<Page<DictTypeVo>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status) {

        Page<DictTypeVo> page = new Page<>(pageNum, pageSize);
        Page<DictTypeVo> result = dictTypeService.pageDictType(page, dictName, dictType, status);
        return Result.ok(result);
    }

    /**
     * 获取所有字典类型（下拉框用）
     */
    @GetMapping("/options")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<List<DictTypeVo>> options() {
        Page<DictTypeVo> page = new Page<>(1, 1000);
        Page<DictTypeVo> result = dictTypeService.pageDictType(page, null, null, 1);
        return Result.ok(result.getRecords());
    }

    /**
     * 获取字典类型详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyAdmin()")
    public Result<DictTypeVo> getById(@PathVariable Long id) {
        DictTypeVo vo = dictTypeService.getDictTypeById(id);
        return Result.ok(vo);
    }

    /**
     * 创建字典类型
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody DictType dictType) {
        dictTypeService.createDictType(dictType);
        return Result.ok();
    }

    /**
     * 更新字典类型
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody DictType dictType) {
        dictTypeService.updateDictType(dictType);
        return Result.ok();
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "字典管理", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        dictTypeService.deleteDictType(id);
        return Result.ok();
    }
}
