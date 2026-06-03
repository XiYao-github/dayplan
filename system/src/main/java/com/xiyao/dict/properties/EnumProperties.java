package com.xiyao.dict.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 枚举模块配置属性
 * <p>
 * 通过 @ConfigurationProperties 绑定前缀为 enum-data 的配置项，
 * 支持在 application.yml 或 application.properties 中配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * # application.yml
 * enum-data:
 *   enable: true                    # 是否启用枚举功能
 * }</pre>
 *
 * @author xiyao
 */
@Data
@ConfigurationProperties(prefix = "enum-data")
public class EnumProperties {

    /**
     * 是否启用枚举功能
     * <p>
     * 设置为 false 时，枚举转换和存储功能将不生效。
     * 默认值：true
     */
    private boolean enable = true;

}