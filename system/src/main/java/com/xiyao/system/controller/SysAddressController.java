package com.xiyao.system.controller;

import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.utils.ConvertUtils;
import com.xiyao.common.utils.data.Result;
import com.xiyao.log.annotation.Log;
import com.xiyao.log.enums.OperationType;
import com.xiyao.system.entity.SysAddress;
import com.xiyao.system.service.ISysAddressService;
import com.xiyao.system.vo.SysAddressVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址信息控制器
 * <p>
 * 提供地址信息的 RESTful API 接口。
 * 查询接口公开访问，增删改接口仅系统管理员可操作。
 * 支持按父级代码获取子级、按级别获取等功能。
 *
 * @author xiyao
 * @see ISysAddressService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/address")
public class SysAddressController extends MyBaseController {

    private final ISysAddressService addressService;

    /**
     * 查询地址列表
     * <p>
     * 支持按名称模糊匹配、按级别筛选、按父级代码筛选。
     * 查询接口公开访问，无需权限控制。
     *
     * @param query 查询条件，包含 name（可选）、level（可选）、parentCode（可选）
     * @return 符合条件的地址信息列表（VO）
     */
    @GetMapping("/list")
    public Result<List<SysAddressVo>> list(SysAddress query) {
        List<SysAddress> list = addressService.list(query);
        return ok(ConvertUtils.sourceToTarget(list, SysAddressVo.class));
    }

    /**
     * 获取地址详情
     * <p>
     * 根据地址代码获取完整地址信息。
     * 查询接口公开访问，无需权限控制。
     *
     * @param code 地址代码（主键）
     * @return 地址详细信息（VO），若不存在则返回 null
     */
    @GetMapping("/{code}")
    public Result<SysAddressVo> getById(@PathVariable Long code) {
        return ok(ConvertUtils.sourceToTarget(addressService.getById(code), SysAddressVo.class));
    }

    /**
     * 创建地址
     * <p>
     * 新增地址信息，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 地址信息视图对象
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "地址管理", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysAddressVo vo) {
        addressService.create(ConvertUtils.sourceToTarget(vo, SysAddress.class));
        return ok();
    }

    /**
     * 更新地址
     * <p>
     * 更新已有地址信息，记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param vo 地址信息视图对象
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "地址管理", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysAddressVo vo) {
        addressService.update(ConvertUtils.sourceToTarget(vo, SysAddress.class));
        return ok();
    }

    /**
     * 删除地址
     * <p>
     * 删除地址信息（物理删除），记录操作日志。
     * 仅系统管理员可访问。
     *
     * @param code 地址代码（主键）
     * @return 操作结果
     */
    @DeleteMapping("/{code}")
    @PreAuthorize("@ss.isSystemAdmin()")
    @Log(module = "地址管理", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long code) {
        addressService.delete(code);
        return ok();
    }

    /**
     * 获取子级地址
     * <p>
     * 根据父级地址代码获取所有直属子级。
     * 查询接口公开访问，无需权限控制。
     *
     * @param parentCode 父级地址代码
     * @return 直属子级地址列表（VO）
     */
    @GetMapping("/children/{parentCode}")
    public Result<List<SysAddressVo>> listByParentCode(@PathVariable Long parentCode) {
        List<SysAddress> list = addressService.listByParentCode(parentCode);
        return ok(ConvertUtils.sourceToTarget(list, SysAddressVo.class));
    }

    /**
     * 按级别获取地址
     * <p>
     * 获取指定级别的所有地址，如获取所有省份。
     * 级别说明：1-省/直辖市、2-市、3-区/县/县级市
     * 查询接口公开访问，无需权限控制。
     *
     * @param level 地址级别（1/2/3）
     * @return 指定级别的地址列表（VO）
     */
    @GetMapping("/level/{level}")
    public Result<List<SysAddressVo>> listByLevel(@PathVariable Integer level) {
        List<SysAddress> list = addressService.listByLevel(level);
        return ok(ConvertUtils.sourceToTarget(list, SysAddressVo.class));
    }
}