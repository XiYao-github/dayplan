package com.xiyao.system.vo;

import lombok.Data;

/**
 * 地址信息视图对象
 * <p>
 * 用于前端展示地址信息。
 *
 * @author xiyao
 */
@Data
public class SysAddressVo {

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
}