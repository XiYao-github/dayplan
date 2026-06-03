package com.xiyao.dict.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.dict.interceptor.DictInterceptor;
import com.xiyao.dict.properties.DictProperties;
import com.xiyao.dict.utils.DictUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 字典模块自动配置类
 * <p>
 * 负责在 Spring Boot 启动时自动装配字典相关的组件。
 * 通过 @ConditionalOnProperty 控制是否启用字典模块。
 *
 * <p>
 * <b>装配条件：</b>
 * <ul>
 *     <li>配置项 dict.enable=true（默认值为 true，可不配置）</li>
 * </ul>
 *
 * <p>
 * <b>自动装配的组件：</b>
 * <ul>
 *     <li>DictUtils：字典缓存管理器（启动时全量加载）</li>
 *     <li>DictInterceptor：MyBatis 拦截器（查询结果自动字典回显）</li>
 * </ul>
 *
 * <p>
 * <b>配置示例（application.yml）：</b>
 * <pre>{@code
 * dict-data:
 *   enable: true  # 默认 true，可不配置
 * }</pre>
 *
 * @author xiyao
 * @see DictProperties
 * @see DictUtils
 * @see DictInterceptor
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(value = "dict-data.enable", havingValue = "true", matchIfMissing = true)
public class DictAutoConfig {

    /**
     * 初始化字典缓存
     * <p>
     * 在应用启动时执行，加载所有字典数据到 DictUtils 缓存中。
     */
    @PostConstruct
    public void initDictCache() {
        // 启动时全量加载字典数据到缓存
        DictUtils.getInstance().loadDictAll();
    }

    /**
     * 注册字典拦截器
     * <p>
     * 将 DictInterceptor 注册到 MyBatis 拦截器链，执行字典回显处理。
     *
     * @return DictInterceptor 实例
     */
    @Bean
    public DictInterceptor dictInterceptor() {
        return new DictInterceptor();
    }
}