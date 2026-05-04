package com.xiyao.encrypt.enums;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * 脱敏策略
 */
@AllArgsConstructor
public enum SensitiveStrategy {

    /**
     * 用户ID
     */
    USER_ID(s -> Convert.toStr(DesensitizedUtil.userId())),

    /**
     * 中文名
     */
    CHINESE_NAME(DesensitizedUtil::chineseName),

    /**
     * 身份证号
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 3, 4)),

    /**
     * 座机号
     */
    FIXED_PHONE(DesensitizedUtil::fixedPhone),

    /**
     * 手机号
     */
    MOBILE_PHONE(DesensitizedUtil::mobilePhone),

    /**
     * 地址
     */
    ADDRESS(s -> DesensitizedUtil.address(s, 8)),

    /**
     * 电子邮件
     */
    EMAIL(DesensitizedUtil::email),

    /**
     * 密码
     */
    PASSWORD(DesensitizedUtil::password),


    /**
     * 中国大陆车牌，包含普通车辆、新能源车辆
     */
    CAR_LICENSE(DesensitizedUtil::carLicense),

    /**
     * 银行卡
     */
    BANK_CARD(DesensitizedUtil::bankCard),

    /**
     * ipv4
     */
    IPV4(DesensitizedUtil::ipv4),

    /**
     * ipv6
     */
    IPV6(DesensitizedUtil::ipv6),


    /**
     * 只显示第一个字符
     */
    FIRST_MASK(DesensitizedUtil::firstMask);


    private final Function<String, String> desensitizer;

    public String apply(String value) {
        return desensitizer.apply(value);
    }
}
