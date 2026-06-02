package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.ConvertUtils;
import com.xiyao.common.utils.data.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.entity.SysRegions;
import com.xiyao.system.service.ISysRegionsService;
import com.xiyao.system.vo.SysRegionsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行政区划控制器
 * <p>
 * 提供行政区划数据的 RESTful API 接口。
 * 查询接口公开访问，增删改接口仅系统管理员可操作。
 * 支持按父级代码获取子级、按级别获取等功能。
 *
 * @author xiyao
 * @see ISysRegionsService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/regions")
public class SysRegionsController extends MyBaseController {

    private final ISysRegionsService regionsService;

    /**
     * 查询行政区划列表
     * <p>
     * 支持按名称模糊匹配、按级别筛选、按父级代码筛选。
     * 查询接口公开访问，无需权限控制。
     *
     * @param query 查询条件，包含 name（可选）、level（可选）、parentCode（可选）
     * @return 符合条件的行政区划列表（VO）
     */
    @GetMapping("/list")
    public Result<List<SysRegionsVo>> list(SysRegions query) {
        List<SysRegions> list = regionsService.list(query);
        return ok(ConvertUtils.sourceToTarget(list, SysRegionsVo.class));
    }

    /**
     * 获取行政区划详情
     * <p>
     * 根据区划代码获取完整行政区划信息。
     * 查询接口公开访问，无需权限控制。
     *
     * @param code 区划代码（主键）
     * @return 行政区划详细信息（VO），若不存在则返回 null
     */
    @GetMapping("/{code}")
    public Result<SysRegionsVo> getById(@PathVariable Long code) {
        return ok(ConvertUtils.sourceToTarget(regionsService.getById(code), SysRegionsVo.class));
    }

    /**
     * 创建行政区划
     * <p>
     * 新增行政区划数据，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 行政区划信息视图对象
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "行政区划", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysRegionsVo vo) {
        regionsService.create(ConvertUtils.sourceToTarget(vo, SysRegions.class));
        return ok();
    }

    /**
     * 更新行政区划
     * <p>
     * 更新已有行政区划信息，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 行政区划信息视图对象
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "行政区划", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysRegionsVo vo) {
        regionsService.update(ConvertUtils.sourceToTarget(vo, SysRegions.class));
        return ok();
    }

    /**
     * 删除行政区划
     * <p>
     * 删除行政区划及其所有下级子节点，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param code 区划代码（主键）
     * @return 操作结果
     */
    @DeleteMapping("/{code}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "行政区划", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long code) {
        regionsService.delete(code);
        return ok();
    }

    /**
     * 获取子级行政区划
     * <p>
     * 根据父级区划代码获取所有直属子级。
     * 查询接口公开访问，无需权限控制。
     *
     * @param parentCode 父级区划代码
     * @return 直属子级行政区划列表（VO）
     */
    @GetMapping("/children/{parentCode}")
    public Result<List<SysRegionsVo>> listByParentCode(@PathVariable Long parentCode) {
        List<SysRegions> list = regionsService.listByParentCode(parentCode);
        return ok(ConvertUtils.sourceToTarget(list, SysRegionsVo.class));
    }

    /**
     * 按级别获取行政区划
     * <p>
     * 获取指定级别的所有行政区划，如获取所有省份。
     * 级别说明：1-省/直辖市、2-市、3-区/县/县级市
     * 查询接口公开访问，无需权限控制。
     *
     * @param level 行政区划级别（1/2/3）
     * @return 指定级别的行政区划列表（VO）
     */
    @GetMapping("/level/{level}")
    public Result<List<SysRegionsVo>> listByLevel(@PathVariable Integer level) {
        List<SysRegions> list = regionsService.listByLevel(level);
        return ok(ConvertUtils.sourceToTarget(list, SysRegionsVo.class));
    }
}