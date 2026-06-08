package com.xiyao.system.vo;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 地址视图对象
 * <p>
 * 用于对外暴露地区信息，作为 AddressUtils 的返回值类型。
 * 每次查询都创建新实例，保证缓存数据不被外部修改。
 *
 * <p>
 * <b>与 SysAddress 的区别：</b>
 * <ul>
 *     <li>SysAddress 是数据库实体，用于内部缓存存储</li>
 *     <li>AddressVo 是视图对象，用于对外暴露数据</li>
 * </ul>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class AddressVo {

    /**
     * 区划代码
     */
    private Long code;

    /**
     * 父级区划代码
     */
    private Long parentCode;

    /**
     * 名称
     */
    private String name;

    /**
     * 省/直辖市代码
     */
    private Long provinceCode;

    /**
     * 省/直辖市名称
     */
    private String provinceName;

    /**
     * 市代码
     */
    private Long cityCode;

    /**
     * 市名称
     */
    private String cityName;

    /**
     * 区/县代码
     */
    private Long areaCode;

    /**
     * 区/县名称
     */
    private String areaName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 级别(1.省/直辖市, 2.市, 3.区/县/地级市)
     */
    private Integer level;

    /**
     * 子地区列表
     */
    private List<AddressVo> children;

    /**
     * 添加子地区
     */
    public void addChild(AddressVo child) {
        if (ObjectUtil.isNull(this.children)) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}