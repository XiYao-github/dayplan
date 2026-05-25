package com.xiyao.dict.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.dict.interceptor.DictResultInterceptor;
import com.xiyao.dict.service.DictService;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 字典模块自动配置类
 * <p>
 * 功能说明：
 * <ol>
 *     <li>启用配置属性类 DictProperties，绑定前缀为 dict 的配置项</li>
 *     <li>注册 MyBatis 字典结果拦截器 DictResultInterceptor</li>
 *     <li>支持通过 dict.enabled=false 禁用字典功能</li>
 * </ol>
 *
 * <p>
 * <b>配置示例：</b>
 * <pre>{@code
 * # application.yml
 * dict:
 *   enabled: true                    # 启用字典功能（默认true）
 *   preload-on-startup: false        # 启动时预加载所有字典（默认false）
 *   cache-strategy: local            # 缓存策略：local（默认local）
 *   load-mode: eager                # 加载模式：eager/lazy（默认eager）
 *   include-codes:                  # 指定加载的字典编码列表
 *     - status
 *     - gender
 *   exclude-codes:                  # 排除的字典编码列表
 * }</pre>
 *
 * @author xiyao
 * @see DictProperties
 * @see DictResultInterceptor
 * @see com.xiyao.dict.config.DictCache
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(value = "dict.enabled", havingValue = "true", matchIfMissing = true)
public class DictAutoConfig {

    /**
     * Spring 配置属性
     * <p>
     * 注入 DictProperties 实例，获取 application.yml 中 dict.* 配置项的值
     */
    @Autowired
    private DictProperties properties;

    /**
     * MyBatis SqlSessionFactory
     * <p>
     * 用于注册 MyBatis 拦截器链，将 DictResultInterceptor 添加到 MyBatis 的拦截器链中
     */
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 项目启动时初始化字典缓存
     * <p>
     * 根据配置决定是否在启动时预加载所有字典数据到缓存。
     * 预加载可以减少首次访问时的延迟，但会增加启动时间。
     * 如果预加载失败，仅记录警告日志，不影响应用启动。
     */
    @PostConstruct
    public void initDictCache() {
        // 仅当配置启动预加载时才执行
        if (properties.isPreloadOnStartup()) {
            try {
                DictCache.getInstance().loadAllDictData();
            } catch (Exception e) {
                // 预加载失败不影响启动，仅记录日志
            }
        }
    }

    /**
     * 创建字典结果拦截器实例
     * <p>
     * 将 DictResultInterceptor 添加到 MyBatis 拦截器链中，
     * 用于拦截查询结果进行字典回显和枚举转换处理。
     *
     * @return 字典结果拦截器实例
     */
    public DictResultInterceptor dictResultInterceptor() {
        return new DictResultInterceptor();
    }
}