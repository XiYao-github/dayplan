package com.xiyao.dict.properties;

import com.xiyao.dict.config.DictAutoConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 字典模块配置属性
 * <p>
 * 通过 @ConfigurationProperties 绑定前缀为 dict-data 的配置项，
 * 支持在 application.yml 或 application.properties 中配置。
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * # application.yml
 * dict-data:
 *   enable: true                    # 是否启用字典功能（默认 true）
 * }</pre>
 *
 * @author xiyao
 * @see DictAutoConfig
 */
@Data
@ConfigurationProperties(prefix = "dict-data")
public class DictProperties {

    /**
     * 是否启用字典功能
     * <p>
     * 设置为 false 时，字典回显功能将不生效，DictInterceptor 不会被加载。
     * 主要用于测试环境或需要临时禁用字典功能的场景。
     *
     * @return true-启用字典功能，false-禁用字典功能
     */
    private boolean enable = true;

}