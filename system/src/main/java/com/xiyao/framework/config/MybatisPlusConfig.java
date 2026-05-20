package com.xiyao.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xiyao.common.base.entity.MyBaseEntity;
import com.xiyao.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ExecutorType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * Mybatis-Plus 配置类
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@MapperScan({"com.xiyao.**.mapper"})
public class MybatisPlusConfig {

    /**
     * Mybatis-Plus 插件拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件（必须配置，且必须指定数据库类型）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型（重要，分页方言）
        paginationInnerInterceptor.setDbType(DbType.MYSQL);

        // 可选：单页分页条数限制（默认无限制）
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 可选：溢出总页数后是否进行处理（默认 false）
        paginationInnerInterceptor.setOverflow(false);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁插件（用于并发更新控制）
        // 注意：实体类中需要添加 @Version 注解的字段
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防止全表更新与删除插件（生产环境强烈建议开启）
        // 当执行不带 where 条件的 update/delete 时会抛出异常
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            /**
             * 获取当前用户（需根据实际认证方式实现） 可从 SecurityContext、ThreadLocal、Request 中获取
             */
            private Long getUserId() {
                return SecurityUtils.getUserId();
            }

            @Override
            public void insertFill(MetaObject metaObject) {
                log.info("开始插入填充...");

                Long userId = getUserId();
                LocalDateTime now = LocalDateTime.now();

                this.strictInsertFill(metaObject, MyBaseEntity.Fields.createBy, Long.class, userId);
                this.strictInsertFill(metaObject, MyBaseEntity.Fields.createTime, LocalDateTime.class, now);

                this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateBy, Long.class, userId);
                this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateTime, LocalDateTime.class, now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                log.info("开始更新填充...");

                Long userId = getUserId();
                LocalDateTime now = LocalDateTime.now();

                this.strictUpdateFill(metaObject, MyBaseEntity.Fields.updateBy, Long.class, userId);
                this.strictUpdateFill(metaObject, MyBaseEntity.Fields.updateTime, LocalDateTime.class, now);
            }
        };
    }

    /**
     * MybatisPlusProperties - 配置属性定制器
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            // Mapper XML 文件位置（支持通配符）
            properties.setMapperLocations(new String[]{"classpath*:mapper/**/*.xml"});
            // 类型别名扫描包
            properties.setTypeAliasesPackage("com.xiyao.**.entity");
            // 类型处理器扫描包
            properties.setTypeHandlersPackage("com.xiyao.**.handler");
            // 默认执行器类型
            properties.setExecutorType(ExecutorType.SIMPLE);
        };
    }

    /**
     * Mybatis 配置定制器
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);    // 驼峰映射
            configuration.setCacheEnabled(true);                // 二级缓存
            configuration.setLazyLoadingEnabled(true);          // 延迟加载
            configuration.setAggressiveLazyLoading(false);      // 非激进加载
            configuration.setDefaultStatementTimeout(30);       // 超时时间
            configuration.setDefaultFetchSize(100);             // 批量获取数量
            configuration.setLogImpl(StdOutImpl.class);         // 日志实现
        };
    }

    /**
     * MyBatis-Plus 特有功能配置
     */
    @Bean
    public GlobalConfig globalConfig() {
        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        // 是否打印 Banner
        globalConfig.setBanner(false);
        // 数据库配置
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        // 主键类型
        dbConfig.setIdType(IdType.AUTO);
        // 表名驼峰映射
        dbConfig.setTableUnderline(true);
        // 逻辑删除全局属性名
        dbConfig.setLogicDeleteField(MyBaseEntity.Fields.deleted);
        // 逻辑删除全局值（默认 1、表示已删除
        dbConfig.setLogicDeleteValue("1");
        // 逻辑未删除全局值（默认 0、表示未删除）
        dbConfig.setLogicNotDeleteValue("0");
        // 设置数据库配置
        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }


}
