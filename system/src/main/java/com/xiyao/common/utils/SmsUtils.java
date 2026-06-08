package com.xiyao.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 短信发送工具类
 * <p>
 * 支持阿里云短信、腾讯云短信等多种短信服务。
 * 通过配置切换不同的短信服务商。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>发送短信：支持模板短信和普通短信</li>
 *     <li>发送验证码：内置验证码短信模板</li>
 *     <li>批量发送：支持群发短信</li>
 * </ul>
 *
 * <p>
 * <b>配置项：</b>
 * <pre>{@code
 * sms:
 *   type: aliyun  # aliyun/tencent
 *   aliyun:
 *     accessKeyId: xxx
 *     accessKeySecret: xxx
 *     signName: Dayplan
 *     templateCode: SMS_xxx
 *   tencent:
 *     appId: xxx
 *     appKey: xxx
 *     signName: Dayplan
 * }</pre>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 发送模板短信
 * Map<String, String> params = Map.of("code", "123456");
 * SmsUtils.send("13800138000", "SMS_xxx", params);
 *
 * // 发送验证码
 * SmsUtils.sendVerifyCode("13800138000", "123456");
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsUtils {

    /**
     * 短信服务商类型
     */
    private static String SMS_TYPE = "aliyun";

    /**
     * 阿里云配置
     */
    private static AliyunConfig ALIYUN_CONFIG = new AliyunConfig();

    /**
     * 腾讯云配置
     */
    private static TencentConfig TENCENT_CONFIG = new TencentConfig();

    /**
     * 默认签名名称
     */
    private static String DEFAULT_SIGN_NAME = "Dayplan";

    /**
     * 设置短信服务商类型
     *
     * @param type 服务商类型：aliyun/tencent
     */
    public static void setSmsType(String type) {
        SMS_TYPE = type;
    }

    /**
     * 设置阿里云配置
     *
     * @param config 阿里云配置
     */
    public static void setAliyunConfig(AliyunConfig config) {
        ALIYUN_CONFIG = config;
    }

    /**
     * 设置腾讯云配置
     *
     * @param config 腾讯云配置
     */
    public static void setTencentConfig(TencentConfig config) {
        TENCENT_CONFIG = config;
    }

    /**
     * 设置默认签名名称
     *
     * @param signName 签名名称
     */
    public static void setDefaultSignName(String signName) {
        DEFAULT_SIGN_NAME = signName;
    }

    /**
     * 发送短信
     *
     * @param phoneNumber  手机号
     * @param templateCode 模板编码
     * @param params       模板参数
     * @return 是否发送成功
     */
    public static boolean send(String phoneNumber, String templateCode, Map<String, String> params) {
        if (StrUtil.isBlank(phoneNumber)) {
            log.error("手机号不能为空");
            return false;
        }

        if (StrUtil.isBlank(templateCode)) {
            log.error("模板编码不能为空");
            return false;
        }

        switch (SMS_TYPE) {
            case "aliyun":
                return sendByAliyun(phoneNumber, templateCode, params);
            case "tencent":
                return sendByTencent(phoneNumber, templateCode, params);
            default:
                log.warn("未配置的短信服务商: {}", SMS_TYPE);
                return false;
        }
    }

    /**
     * 发送验证码短信
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return 是否发送成功
     */
    public static boolean sendVerifyCode(String phoneNumber, String code) {
        // 参数校验
        if (StrUtil.isBlank(code)) {
            log.error("发送验证码失败：验证码不能为空");
            return false;
        }
        // 使用配置的验证码模板
        String templateCode = ALIYUN_CONFIG.getTemplateCode();
        if (StrUtil.isBlank(templateCode)) {
            // 使用腾讯云模板
            templateCode = TENCENT_CONFIG.getTemplateCode();
        }

        Map<String, String> params = Map.of("code", code);
        return send(phoneNumber, templateCode, params);
    }

    /**
     * 批量发送短信
     *
     * @param phoneNumbers 手机号列表
     * @param templateCode 模板编码
     * @param params       模板参数（为空时使用默认参数）
     * @return 成功发送的数量
     */
    public static int sendBatch(String[] phoneNumbers, String templateCode, Map<String, String> params) {
        int successCount = 0;
        for (String phone : phoneNumbers) {
            if (send(phone, templateCode, params)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 发送营销短信
     *
     * @param phoneNumber 手机号
     * @param content     短信内容
     * @return 是否发送成功
     */
    public static boolean sendMarketing(String phoneNumber, String content) {
        // 营销短信通常使用不同的模板
        String templateCode = ALIYUN_CONFIG.getMarketingTemplateCode();
        if (StrUtil.isBlank(templateCode)) {
            log.error("营销短信模板未配置");
            return false;
        }

        // 营销短信模板参数格式可能不同
        Map<String, String> params = Map.of("content", content);
        return send(phoneNumber, templateCode, params);
    }

    /**
     * 通过阿里云发送短信
     */
    private static boolean sendByAliyun(String phoneNumber, String templateCode, Map<String, String> params) {
        // TODO: 集成阿里云短信 SDK
        // 实际实现参考：
        // https://help.aliyun.com/document_detail/112820.html
        log.info("通过阿里云发送短信: phone={}, template={}, params={}", phoneNumber, templateCode, params);

        // 模拟发送成功
        // 实际实现需要：
        // 1. DefaultProfile.addEndpoint()
        // 2. new DefaultAcsClient(profile)
        // 3. SendSmsRequest -> sendSmsRequest.setPhoneNumbers(phoneNumber)
        // 4. sendSmsRequest.setSignName(DEFAULT_SIGN_NAME)
        // 5. sendSmsRequest.setTemplateCode(templateCode)
        // 6. sendSmsRequest.setTemplateParam(JSON.toJSONString(params))

        return true;
    }

    /**
     * 通过腾讯云发送短信
     */
    private static boolean sendByTencent(String phoneNumber, String templateCode, Map<String, String> params) {
        // TODO: 集成腾讯云短信 SDK
        // 实际实现参考：
        // https://cloud.tencent.com/document/product/382/43197
        log.info("通过腾讯云发送短信: phone={}, template={}, params={}", phoneNumber, templateCode, params);

        // 模拟发送成功
        // 实际实现需要：
        // 1. SmsAppId = ALIYUN_CONFIG.getAppId()
        // 2. SmsSingleSender sender = new SmsSingleSender(appId, appKey)
        // 3. SmsSingleSenderResult result = sender.send(0, "86", phoneNumber, templateCode, "", params)

        return true;
    }

    /**
     * 阿里云短信配置
     */
    @Data
    public static class AliyunConfig {
        /**
         * AccessKey ID
         */
        private String accessKeyId;

        /**
         * AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * 短信签名名称
         */
        private String signName = DEFAULT_SIGN_NAME;

        /**
         * 短信模板编码
         */
        private String templateCode;

        /**
         * 营销短信模板编码
         */
        private String marketingTemplateCode;

        /**
         * 区域节点
         */
        private String regionId = "cn-hangzhou";

        /**
         * 产品域名
         */
        private String domain = "dysmsapi.aliyuncs.com";
    }

    /**
     * 腾讯云短信配置
     */
    @Data
    public static class TencentConfig {
        /**
         * App ID
         */
        private String appId;

        /**
         * App Key
         */
        private String appKey;

        /**
         * 短信签名名称
         */
        private String signName = DEFAULT_SIGN_NAME;

        /**
         * 短信模板编码
         */
        private String templateCode;

        /**
         * 营销短信模板编码
         */
        private String marketingTemplateCode;
    }
}