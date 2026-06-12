package com.xiyao.log.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志模块配置属性
 * <p>
 * 通过 @ConfigurationProperties 绑定前缀为 system.log 的配置项，
 * 支持在 application.yml 或 application.properties 中配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * # application.yml
 * system:
 *   log:
 *     enable: true                    # 是否启用日志功能
 * }</pre>
 *
 * @author xiyao
 * @see LogProperties
 */
@Data
@ConfigurationProperties(prefix = "system.log")
public class LogProperties {

    /**
     * 是否启用日志功能
     * <p>
     * 设置为 false 时，日志记录功能将不生效。
     * 默认值：true
     */
    private boolean enable = true;

}