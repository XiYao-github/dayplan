package com.xiyao.dict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
 *   enabled: true                    # 是否启用字典功能
 *   preload-on-startup: false      # 启动时预加载所有字典
 *   cache-strategy: local           # 缓存策略（预留）
 *   load-mode: eager               # 加载模式：eager/lazy
 *   include-codes:                 # 指定加载的字典编码
 *     - status
 *     - gender
 *   exclude-codes:                 # 排除的字典编码
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
    private boolean enabled = true;

    /**
     * 缓存策略
     * <p>
     * 预留配置项，当前版本仅支持 local（本地内存缓存）。
     * 后续可扩展 caffeine（本地高性能缓存）或 redis（分布式缓存）。
     * 默认值：local
     */
    private String cacheStrategy = "local";

    /**
     * 字典加载模式
     * <p>
     * <ul>
     *     <li>eager：启动时加载所有字典到缓存，查询快但启动慢</li>
     *     <li>lazy：首次查询时加载，按需加载但首次查询慢</li>
     * </ul>
     * 默认值：eager
     */
    private String loadMode = "eager";

    /**
     * 启动时是否预加载所有字典
     * <p>
     * 设置为 true 时，项目启动时将所有字典数据加载到缓存，
     * 适用于字典数据量不大的场景，可以减少首次查询延迟。
     * 默认值：false
     */
    private boolean preloadOnStartup = false;

    /**
     * 需要加载的字典类型编码列表
     * <p>
     * 如果不为空，则只加载列表中的字典编码。
     * 为空表示加载所有字典。
     * 示例：["status", "gender", "nation"]
     */
    private List<String> includeCodes = new ArrayList<>();

    /**
     * 排除的字典类型编码列表
     * <p>
     * 指定不加载的字典编码，优先级高于 includeCodes。
     * 示例：["internal_status"] 排除内部状态字典
     */
    private List<String> excludeCodes = new ArrayList<>();
}