package com.xiyao.crypto.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xiyao.crypto.enums.SensitiveStrategy;
import com.xiyao.crypto.serialize.SensitiveSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 * <p>
 * 用于标记需要脱敏处理的字段，在 JSON 序列化时自动应用脱敏策略。
 * 支持手机号、身份证号、银行卡、姓名、地址等多种脱敏方式。
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     // 手机号脱敏，显示前三后四
 *     @Sensitive(SensitiveStrategy.MOBILE_PHONE)
 *     private String phone;
 *
 *     // 身份证号脱敏，显示前三后四
 *     @Sensitive(SensitiveStrategy.ID_CARD)
 *     private String idCard;
 *
 *     // 银行卡脱敏
 *     @Sensitive(SensitiveStrategy.BANK_CARD)
 *     private String bankCard;
 *
 *     // 中文名脱敏，只显示第一个字
 *     @Sensitive(SensitiveStrategy.CHINESE_NAME)
 *     private String name;
 * }
 * }</pre>
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>Jackson 序列化对象时，会调用 SensitiveSerializer 进行处理</li>
 *     <li>Serializer 根据 @SensitiveField 注解的策略调用对应的脱敏方法</li>
 *     <li>脱敏后的数据写入 JSON，不会影响数据库中的原始数据</li>
 * </ol>
 * <p>
 * <b>适用场景：</b>
 * <ul>
 *     <li>API 接口返回敏感数据时自动脱敏</li>
 *     <li>日志输出时隐藏敏感信息</li>
 *     <li>导出数据时进行脱敏处理</li>
 * </ul>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>仅对 String 类型字段生效</li>
 *     <li>脱敏发生在序列化阶段，原始数据不受影响</li>
 *     <li>需要 Jackson 进行 JSON 序列化才会触发</li>
 * </ul>
 *
 * @author xiyao
 * @see SensitiveStrategy
 * @see SensitiveSerializer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface SensitiveField {

    /**
     * 脱敏策略
     * <p>
     * 指定字段使用的脱敏策略，不同策略对应不同的脱敏规则。
     *
     * @return 脱敏策略枚举
     */
    SensitiveStrategy value();

}