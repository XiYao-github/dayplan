package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.ConvertUtils;
import com.xiyao.common.utils.data.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.entity.SysConfig;
import com.xiyao.system.service.ISysConfigService;
import com.xiyao.system.vo.SysConfigVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 * <p>
 * 提供系统配置项的 RESTful API 接口，仅系统管理员可操作。
 * 配置项用于存储系统运行时可动态调整的参数。
 *
 * @author xiyao
 * @see ISysConfigService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends MyBaseController {

    private final ISysConfigService configService;

    /**
     * 查询配置列表
     * <p>
     * 支持按配置名称模糊匹配、按状态精确筛选。
     * 仅系统管理员可访问。
     *
     * @param query 查询条件，包含 name（可选）、status（可选）
     * @return 符合条件的配置列表（VO）
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result<List<SysConfigVo>> list(SysConfig query) {
        List<SysConfig> list = configService.list(query);
        return ok(ConvertUtils.sourceToTarget(list, SysConfigVo.class));
    }

    /**
     * 获取配置详情
     * <p>
     * 根据配置ID获取完整配置信息。
     * 仅系统管理员可访问。
     *
     * @param id 配置主键ID
     * @return 配置详细信息（VO），若不存在则返回 null
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result<SysConfigVo> getById(@PathVariable Long id) {
        return ok(ConvertUtils.sourceToTarget(configService.getById(id), SysConfigVo.class));
    }

    /**
     * 根据配置名称获取配置值
     * <p>
     * 用于前端动态获取配置项的值，如开关状态、阈值等。
     * 仅系统管理员可访问。
     *
     * @param name 配置名称（唯一标识）
     * @return 配置值，若配置不存在则返回 null
     */
    @GetMapping("/key/{name}")
    @PreAuthorize("@ss.isSystemAdmin()")
    public Result<String> getByName(@PathVariable String name) {
        SysConfig config = configService.lambdaQuery()
                .eq(SysConfig::getName, name)
                .eq(SysConfig::getDeleted, 0)
                .one();
        return ok(config != null ? config.getValue() : null);
    }

    /**
     * 创建配置
     * <p>
     * 新增系统配置项，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 配置信息视图对象
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "系统配置", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysConfigVo vo) {
        configService.create(ConvertUtils.sourceToTarget(vo, SysConfig.class));
        return ok();
    }

    /**
     * 更新配置
     * <p>
     * 更新已有配置项的值和状态，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 配置信息视图对象
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "系统配置", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysConfigVo vo) {
        configService.update(ConvertUtils.sourceToTarget(vo, SysConfig.class));
        return ok();
    }

    /**
     * 删除配置
     * <p>
     * 执行逻辑删除，将 deleted 字段置为 1，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param id 配置主键ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "系统配置", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ok();
    }
}