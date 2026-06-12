package com.xiyao.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置属性
 * <p>
 * 绑定配置前缀 system.common.oss，用于配置阿里云 OSS 存储服务。
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * system:
 *   common:
 *     oss:
 *       accessKeyId: "your-access-key-id"
 *       accessKeySecret: "your-access-key-secret"
 *       endpoint: "oss-cn-hangzhou.aliyuncs.com"
 *       bucketName: "your-bucket-name"
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "system.common.oss")
public class OssProperties {

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * OSS 服务节点地址
     */
    private String endpoint;

    /**
     * 存储桶名称
     */
    private String bucketName;
}
