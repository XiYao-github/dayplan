package com.xiyao.system.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 系统用户
 * </p>
 *
 * @author xiyao
 * @since 2026-04-26
 */
@Data
@Accessors(chain = true)
public class SysUserVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

}
