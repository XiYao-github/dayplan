package com.xiyao.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信配置属性
 * <p>
 * 绑定配置前缀 system.common.sms，用于配置阿里云短信服务。
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   common:
 *     sms:
 *       accessKeyId: "your-access-key-id"
 *       accessKeySecret: "your-access-key-secret"
 *       endpoint: "dysmsapi.aliyuncs.com"
 *       signName: "签名名称"
 *       templateCode: "SMS_xxx"
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "system.common.sms")
public class SmsProperties {

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 短信服务节点地址
     */
    private String endpoint;

    /**
     * 短信签名名称
     */
    private String signName;

    /**
     * 短信模板编码
     */
    private String templateCode;
}
