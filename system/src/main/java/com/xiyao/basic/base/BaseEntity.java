package com.xiyao.basic.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiyao.basic.constant.Constant;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = Constant.DATE_TIME)
    @JsonFormat(timezone = Constant.TIME_ZONE, pattern = Constant.DATE_TIME)
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = Constant.DATE_TIME)
    @JsonFormat(timezone = Constant.TIME_ZONE, pattern = Constant.DATE_TIME)
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除时间
     */
    @DateTimeFormat(pattern = Constant.DATE_TIME)
    @JsonFormat(timezone = Constant.TIME_ZONE, pattern = Constant.DATE_TIME)
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 删除标志(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    /**
     * 乐观锁
     */
    //@Version
    //@TableField("version")
    // private Integer version;
}