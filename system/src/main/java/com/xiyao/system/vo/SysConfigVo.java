package com.xiyao.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置视图对象
 * <p>
 * 用于前端展示系统配置信息。
 *
 * @author xiyao
 */
@Data
public class SysConfigVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 参数名
     */
    private String name;

    /**
     * 参数值
     */
    private String value;

    /**
     * 状态(0.停用 1.正常)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}