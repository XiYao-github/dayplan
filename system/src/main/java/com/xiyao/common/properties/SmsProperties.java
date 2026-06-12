package com.xiyao.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "system.common.sms")
public class SmsProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint;

    private String signName;

    private String templateCode;

}
