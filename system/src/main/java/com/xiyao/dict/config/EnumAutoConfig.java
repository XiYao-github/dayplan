package com.xiyao.dict.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.dict.properties.EnumProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 枚举模块自动配置类
 * <p>
 * 负责在 Spring Boot 启动时自动装配枚举相关的组件。
 * 通过 @ConditionalOnProperty 控制是否启用枚举模块。
 *
 * <p>
 * <b>装配条件：</b>
 * <ul>
 *     <li>配置项 enum-data.enable=true（默认值为 true，可不配置）</li>
 * </ul>
 *
 * <p>
 * <b>自动装配的组件：</b>
 * <ul>
 *     <li>EnumTypeHandler：枚举类型处理器（数据库存取）</li>
 * </ul>
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * enum-data:
 *   enable: true  # 默认 true，可不配置
 * }</pre>
 *
 * @author xiyao
 * @see EnumProperties
 */
@Configuration
@EnableConfigurationProperties(EnumProperties.class)
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@ConditionalOnProperty(value = "enum-data.enable", havingValue = "true", matchIfMissing = true)
public class EnumAutoConfig {

}