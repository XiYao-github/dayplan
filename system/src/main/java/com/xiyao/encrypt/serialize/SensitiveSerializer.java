package com.xiyao.encrypt.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.xiyao.encrypt.annotation.Sensitive;
import com.xiyao.encrypt.enums.SensitiveStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 数据脱敏序列化器
 * <p>
 * Jackson JSON 序列化器，用于在 JSON 序列化时对敏感数据进行脱敏处理。
 * 实现 ContextualSerializer 接口，可以在序列化前根据字段注解动态获取脱敏策略。
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>Jackson 在序列化前调用 createContextual() 方法获取上下文信息</li>
 *     <li>从字段注解中读取 @Sensitive 注解，获取脱敏策略</li>
 *     <li>创建带有策略信息的序列化器实例</li>
 *     <li>序列化时调用 serialize() 方法，对值进行脱敏后输出</li>
 * </ol>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * public class User {
 *     // 手机号自动脱敏：13800138000 → 138****8000
 *     @Sensitive(SensitiveStrategy.MOBILE_PHONE)
 *     private String phone;
 *
 *     // 身份证号自动脱敏：110101199001011234 → 110***********1234
 *     @Sensitive(SensitiveStrategy.ID_CARD)
 *     private String idCard;
 *
 *     // 银行卡自动脱敏：6222021234567890 → 6222*****7890
 *     @Sensitive(SensitiveStrategy.BANK_CARD)
 *     private String bankCard;
 * }
 *
 * // 序列化时自动脱敏
 * User user = new User();
 * user.setPhone("13800138000");
 * ObjectMapper mapper = new ObjectMapper();
 * String json = mapper.writeValueAsString(user);
 * // {"phone":"138****8000","idCard":"110***********1234","bankCard":"6222*****7890"}
 * }</pre>
 * <p>
 * <b>特点：</b>
 * <ul>
 *     <li>零侵入：对业务代码无感知，不需要手动调用脱敏方法</li>
 *     <li>只影响序列化：数据库中存储的是原始完整数据</li>
 *     <li>支持多种脱敏策略：手机号、身份证、银行卡、姓名等</li>
 * </ul>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>只能用于 String 类型字段</li>
 *     <li>需要 Jackson 进行 JSON 序列化才会触发</li>
 *     <li>脱敏在序列化阶段发生，不影响数据库原始数据</li>
 * </ul>
 *
 * @author xiyao
 * @see Sensitive
 * @see SensitiveStrategy
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    /**
     * 脱敏策略
     * <p>
     * 存储当前字段使用的脱敏策略，由 createContextual() 方法设置。
     */
    private SensitiveStrategy strategy;

    /**
     * 序列化方法
     * <p>
     * 对字段值进行脱敏处理后写入 JSON。
     *
     * @param value      字段值（原始数据）
     * @param gen        JSON 生成器
     * @param serializers 序列化器提供者
     * @throws IOException 如果写入失败
     */
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 判断是否需要进行脱敏
        if (strategy != null && value != null) {
            // 调用策略的 apply 方法进行脱敏
            String desensitized = strategy.apply(value);
            // 将脱敏后的值写入 JSON
            gen.writeString(desensitized);
        } else {
            // 不使用策略（value 为空或策略为空），写入原值
            gen.writeString(value);
        }
    }

    /**
     * 创建上下文相关的序列化器
     * <p>
     * 在序列化前被调用，根据字段注解信息创建带有策略的序列化器实例。
     * <p>
     * <b>处理逻辑：</b>
     * <ol>
     *     <li>从 BeanProperty 获取字段上的 @Sensitive 注解</li>
     *     <li>检查字段类型是否为 String 或其子类</li>
     *     <li>如果是，读取脱敏策略，创建新的带策略序列化器</li>
     *     <li>否则，返回标准序列化器（不解密）</li>
     * </ol>
     *
     * @param prov     序列化器提供者
     * @param property 字段属性信息
     * @return 上下文相关的序列化器
     * @throws JsonMappingException 如果创建失败
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        // 获取字段上的 @Sensitive 注解
        Sensitive annotation = property.getAnnotation(Sensitive.class);

        // 检查注解存在且字段类型为 String
        if (annotation != null && property.getType().isTypeOrSubTypeOf(String.class)) {
            // 读取注解配置的脱敏策略
            SensitiveStrategy strategy = annotation.value();
            // 创建带有策略的序列化器
            return new SensitiveSerializer(strategy);
        }

        // 没有注解或类型不匹配，返回标准序列化器
        return prov.findValueSerializer(property.getType(), property);
    }

}