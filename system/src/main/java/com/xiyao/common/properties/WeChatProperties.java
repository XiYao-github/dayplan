package com.xiyao.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "system.common.wechat")
public class WeChatProperties {

    private String appId;

    private String secret;
}
