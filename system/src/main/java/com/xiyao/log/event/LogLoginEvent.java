package com.xiyao.log.event;

import com.xiyao.common.base.event.MyBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 登录日志事件
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogLoginEvent extends MyBaseEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录状态(0.失败 1.成功)
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

}
