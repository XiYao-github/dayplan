package com.xiyao.dict.properties;

import com.xiyao.dict.config.DictAutoConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 字典模块配置属性
 * <p>
 * 通过 @ConfigurationProperties 绑定前缀为 dict 的配置项，
 * 支持在 application.yml 或 application.properties 中配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * # application.yml
 * dict:
 *   enable: true                    # 是否启用字典功能
 * }</pre>
 *
 * @author xiyao
 * @see DictAutoConfig
 */
@Data
@ConfigurationProperties(prefix = "dict")
public class DictProperties {

    /**
     * 是否启用字典功能
     * <p>
     * 设置为 false 时，字典回显和枚举转换功能将不生效。
     * 默认值：true
     */
    private boolean enable = true;

}