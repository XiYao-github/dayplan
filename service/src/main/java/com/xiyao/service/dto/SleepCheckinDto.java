package com.xiyao.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * <p>
 * 睡觉打卡请求
 * </p>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public class SleepCheckinDto {

    /**
     * 上床时间
     */
    private LocalTime bedTime;

    /**
     * 备注
     */
    private String remark;
}
