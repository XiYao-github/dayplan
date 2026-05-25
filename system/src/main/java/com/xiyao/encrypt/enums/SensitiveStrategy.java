package com.xiyao.encrypt.enums;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * 数据脱敏策略枚举
 * <p>
 * 定义各种敏感数据的脱敏规则，支持用户ID、身份证、银行卡、手机号、姓名等多种类型。
 * <p>
 * <b>脱敏效果示例：</b>
 * <table border="1">
 *     <tr><th>策略</th><th>原始数据</th><th>脱敏后</th></tr>
 *     <tr><td>MOBILE_PHONE</td><td>13800138000</td><td>138****8000</td></tr>
 *     <tr><td>ID_CARD</td><td>110101199001011234</td><td>110***********1234</td></tr>
 *     <tr><td>BANK_CARD</td><td>6222021234567890</td><td>6222*****7890</td></tr>
 *     <tr><td>CHINESE_NAME</td><td>张三</td><td>张*</td></tr>
 *     <tr><td>EMAIL</td><td>test@example.com</td><td>t**@example.com</td></tr>
 * </table>
 * <p>
 * <b>使用方式：</b>
 * 在实体类字段上添加 @Sensitive(SensitiveStrategy.XXX) 注解，
 * Jackson 序列化时会自动应用脱敏规则。
 *
 * @author xiyao
 * @see com.xiyao.encrypt.annotation.Sensitive
 */
@AllArgsConstructor
public enum SensitiveStrategy {

    /**
     * 用户ID脱敏
     * <p>
     * 将用户ID转换为字符串形式返回，用于展示场景。
     *
     * @param s 原始用户ID
     * @return 脱敏后的字符串
     */
    USER_ID(s -> Convert.toStr(DesensitizedUtil.userId())),

    /**
     * 中文姓名脱敏
     * <p>
     * 只显示第一个字符，其余用 * 替换。例如：张三 → 张*
     *
     * @param s 原始姓名
     * @return 脱敏后的姓名
     */
    CHINESE_NAME(DesensitizedUtil::chineseName),

    /**
     * 身份证号脱敏
     * <p>
     * 显示前三后四，中间隐藏。例如：110101199001011234 → 110***********1234
     *
     * @param s 原始身份证号
     * @return 脱敏后的身份证号
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 3, 4)),

    /**
     * 座机号脱敏
     * <p>
     * 座机号码脱敏，显示区号和后四位，中间隐藏。
     *
     * @param s 原始座机号
     * @return 脱敏后的座机号
     */
    FIXED_PHONE(DesensitizedUtil::fixedPhone),

    /**
     * 手机号脱敏
     * <p>
     * 显示前三后四，中间隐藏。例如：13800138000 → 138****8000
     *
     * @param s 原始手机号
     * @return 脱敏后的手机号
     */
    MOBILE_PHONE(DesensitizedUtil::mobilePhone),

    /**
     * 地址脱敏
     * <p>
     * 显示前八字符，其余隐藏。适用于详细地址的脱敏处理。
     *
     * @param s 原始地址
     * @return 脱敏后的地址
     */
    ADDRESS(s -> DesensitizedUtil.address(s, 8)),

    /**
     * 电子邮箱脱敏
     * <p>
     * 只显示邮箱的用户名前缀第一个字符和域名部分。
     * 例如：test@example.com → t**@example.com
     *
     * @param s 原始邮箱
     * @return 脱敏后的邮箱
     */
    EMAIL(DesensitizedUtil::email),

    /**
     * 密码脱敏
     * <p>
     * 所有字符替换为 *，完全不显示。
     *
     * @param s 原始密码
     * @return 脱敏后的密码（全 *）
     */
    PASSWORD(DesensitizedUtil::password),


    /**
     * 中国大陆车牌脱敏
     * <p>
     * 支持普通车辆和新能源车辆号牌，显示省份简称和后几位。
     *
     * @param s 原始车牌号
     * @return 脱敏后的车牌号
     */
    CAR_LICENSE(DesensitizedUtil::carLicense),

    /**
     * 银行卡号脱敏
     * <p>
     * 显示前四后四，中间隐藏。例如：6222021234567890 → 6222*****7890
     *
     * @param s 原始银行卡号
     * @return 脱敏后的银行卡号
     */
    BANK_CARD(DesensitizedUtil::bankCard),

    /**
     * IPv4 地址脱敏
     * <p>
     * 保留第一段，其余用 * 替换。例如：192.168.1.100 → 192.***.***.***
     *
     * @param s 原始 IPv4 地址
     * @return 脱敏后的地址
     */
    IPV4(DesensitizedUtil::ipv4),

    /**
     * IPv6 地址脱敏
     * <p>
     * IPv6 地址的脱敏处理。
     *
     * @param s 原始 IPv6 地址
     * @return 脱敏后的地址
     */
    IPV6(DesensitizedUtil::ipv6),


    /**
     * 只显示第一个字符
     * <p>
     * 通用脱敏策略，只保留字符串的第一个字符，其余全部替换为 *。
     * 适用于没有特定脱敏规则的数据。
     *
     * @param s 原始字符串
     * @return 脱敏后的字符串
     */
    FIRST_MASK(DesensitizedUtil::firstMask);


    /**
     * 脱敏函数
     * <p>
     * 接收原始字符串，返回脱敏后的字符串。
     * 使用 Function 函数接口，方便统一处理。
     */
    private final Function<String, String> desensitizer;

    /**
     * 应用脱敏策略
     * <p>
     * 对给定的原始值应用脱敏规则，返回脱敏后的值。
     *
     * @param value 原始字符串
     * @return 脱敏后的字符串
     */
    public String apply(String value) {
        return desensitizer.apply(value);
    }
}