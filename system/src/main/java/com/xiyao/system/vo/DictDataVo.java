package com.xiyao.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据视图对象
 *
 * @author xiyao
 */
@Data
public class DictDataVo {

    private Long id;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 状态（0=停用 1=正常）
     */
    private Integer status;

    /**
     * 是否默认（0=否 1=是）
     */
    private Integer isDefault;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}