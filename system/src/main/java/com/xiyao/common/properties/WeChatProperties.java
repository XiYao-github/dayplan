package com.xiyao.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信配置属性
 * <p>
 * 绑定配置前缀 system.common.wechat，用于配置微信小程序的认证参数。
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   common:
 *     wechat:
 *       appId: "your-app-id"
 *       secret: "your-secret"
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "system.common.wechat")
public class WeChatProperties {

    /**
     * 微信小程序应用唯一标识
     */
    private String appId;

    /**
     * 微信小程序应用密钥
     */
    private String secret;
}
