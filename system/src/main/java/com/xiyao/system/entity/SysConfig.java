package com.xiyao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * <p>
 * 系统配置
 * </p>
 *
 * @author xiyao
 * @since 2026-06-09
 */
@Data
@Accessors(chain = true)
@TableName("sys_config")
public class SysConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 参数名
     */
    @TableField("name")
    private String name;

    /**
     * 参数值
     */
    @TableField("value")
    private String value;

    /**
     * 状态(0.停用 1.正常)
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除(0.未删除 1.已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁
     */
    @Version
    @TableField("version")
    private Integer version;
}
