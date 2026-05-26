package com.xiyao.dict.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.xiyao.dict.interceptor.DictResultInterceptor;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 字典模块自动配置类
 *
 * @author xiyao
 * @see DictProperties
 * @see DictResultInterceptor
 * @see DictCache
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(value = "dict.enabled", havingValue = "true", matchIfMissing = true)
public class DictAutoConfig {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private DictProperties properties;

    @PostConstruct
    public void initDictCache() {
        if (properties.isPreloadOnStartup()) {
            try {
                DictCache.getInstance().loadAllDictData();
            } catch (Exception e) {
                // 预加载失败不影响启动
            }
        }
    }

    /**
     * 注册字典结果拦截器到 MyBatis
     * <p>
     * 将拦截器添加到 MyBatis 拦截器链后注册为 Bean
     */
    @Bean
    public Interceptor dictResultInterceptor() {
        Interceptor interceptor = new DictResultInterceptor();
        // 添加到 MyBatis 拦截器链（必须在其他 Bean 注册之前）
        sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        return interceptor;
    }
}
