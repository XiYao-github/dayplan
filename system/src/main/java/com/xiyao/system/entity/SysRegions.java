package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 行政区划
 * </p>
 *
 * @author xiyao
 * @since 2026-04-26
 */
@Data
@Accessors(chain = true)
@TableName("sys_regions")
public class SysRegions {

    /**
     * 区划代码
     */
    @TableId(value = "code", type = IdType.AUTO)
    private Long code;

    /**
     * 父级区划代码
     */
    @TableField("parent_code")
    private Long parentCode;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 省/直辖市代码
     */
    @TableField("province_code")
    private Long provinceCode;

    /**
     * 省/直辖市名称
     */
    @TableField("province_name")
    private String provinceName;

    /**
     * 市代码
     */
    @TableField("city_code")
    private Long cityCode;

    /**
     * 市名称
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 区/县代码
     */
    @TableField("area_code")
    private Long areaCode;

    /**
     * 区/县名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 级别(1.省/直辖市, 2.市, 3.区/县/地级市)
     */
    @TableField("level")
    private Integer level;

    /**
     * 删除标志(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("del_flag")
    private Byte delFlag;
}
