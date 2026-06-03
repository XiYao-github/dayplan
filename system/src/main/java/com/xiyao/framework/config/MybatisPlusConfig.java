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
 * MyBatis-Plus 配置类
 * <p>
 * 功能：
 * <ol>
 *     <li>配置分页插件，支持多数据库类型</li>
 *     <li>配置乐观锁插件（@Version 注解）</li>
 *     <li>防止全表更新/删除（生产环境安全防护）</li>
 *     <li>自动填充创建人/创建时间/更新人/更新时间</li>
 *     <li>配置驼峰映射、逻辑删除等全局设置</li>
 * </ol>
 *
 * <p>
 * <b>重要配置说明：</b>
 * <ul>
 *     <li>分页插件必须指定 DbType，否则无法正确生成分页 SQL</li>
 *     <li>BlockAttackInnerInterceptor 强烈建议在生产环境开启，防止误操作全表</li>
 *     <li>自动填充依赖 SecurityUtils.getUserId() 获取当前登录用户</li>
 * </ul>
 *
 * @author xiyao
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@MapperScan({"com.xiyao.**.mapper"})
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 插件拦截器链
     * <p>
     * 包含分页、乐观锁、防全表操作等插件，按添加顺序执行。
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ========== 分页插件 ==========
        // PaginationInnerInterceptor：实现分页查询，必须配置且指定数据库类型
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型（重要！分页方言必须与实际数据库匹配）
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 单页最大条数限制（默认无限制，建议设置防止一次性查询过多数据）
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 溢出总页数后是否进行处理（默认 false：超出则回绕到第一页）
        paginationInnerInterceptor.setOverflow(false);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // ========== 乐观锁插件 ==========
        // OptimisticLockerInnerInterceptor：并发更新时防止覆盖
        // 使用前提：实体类中需要使用 @Version 注解标注版本号字段
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // ========== 防全表操作插件 ==========
        // BlockAttackInnerInterceptor：禁止不带 WHERE 条件的 UPDATE/DELETE
        // 生产环境强烈建议开启，防止误操作导致数据灾难
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     * <p>
     * 在插入和更新时自动填充创建人、创建时间、修改人、修改时间等字段。
     * 依赖 SecurityUtils.getUserId() 获取当前登录用户。
     *
     * @return MetaObjectHandler 实例
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            /**
             * 获取当前登录用户的 ID
             * <p>
             * 从 SecurityContext、ThreadLocal 或 Request 中获取。
             * 如果当前无登录用户，返回 null。
             *
             * @return 当前用户 ID，未登录返回 null
             */
            private Long getUserId() {
                return SecurityUtils.getUserId();
            }

            /**
             * 插入操作自动填充
             * <p>
             * 填充字段：createBy、createTime、updateBy、updateTime
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                log.info("开始插入填充...");

                Long userId = getUserId();  // 获取当前用户ID
                LocalDateTime now = LocalDateTime.now();  // 获取当前时间

                // 严格插入填充：只有当字段为空时才填充（避免覆盖已有值）
                this.strictInsertFill(metaObject, MyBaseEntity.Fields.createBy, Long.class, userId);
                this.strictInsertFill(metaObject, MyBaseEntity.Fields.createTime, LocalDateTime.class, now);

                this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateBy, Long.class, userId);
                this.strictInsertFill(metaObject, MyBaseEntity.Fields.updateTime, LocalDateTime.class, now);
            }

            /**
             * 更新操作自动填充
             * <p>
             * 填充字段：updateBy、updateTime
             * 不填充 createBy 和 createTime（这些只在插入时填充一次）
             */
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
     * MyBatis-Plus 配置属性定制器
     * <p>
     * 配置 Mapper XML 路径、类型别名、类型处理器等。
     *
     * @return MybatisPlusPropertiesCustomizer 实例
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            // Mapper XML 文件位置（支持 classpath*: 和通配符）
            properties.setMapperLocations(new String[]{"classpath*:mapper/**/*.xml"});
            // 类型别名扫描包（实体类的简短类名）
            properties.setTypeAliasesPackage("com.xiyao.**.entity");
            // 类型处理器扫描包（用于自动转换 Java 类型和数据库类型）
            // properties.setTypeHandlersPackage("com.xiyao.**.handler");
            // 默认执行器类型（SIMPLE：简单执行器，逐条执行）
            properties.setExecutorType(ExecutorType.SIMPLE);
        };
    }

    /**
     * MyBatis 核心配置定制器
     * <p>
     * 配置驼峰映射、缓存、超时等核心行为。
     *
     * @return ConfigurationCustomizer 实例
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 开启驼峰映射：数据库 user_name -> Java userName
            configuration.setMapUnderscoreToCamelCase(true);
            // 开启 MyBatis 二级缓存
            configuration.setCacheEnabled(true);
            // 开启延迟加载（按需加载关联对象）
            configuration.setLazyLoadingEnabled(true);
            // 非激进加载：调用对象方法时才加载（不是获取属性就加载）
            configuration.setAggressiveLazyLoading(false);
            // 默认语句超时时间（秒）
            configuration.setDefaultStatementTimeout(30);
            // 默认批量获取数量
            configuration.setDefaultFetchSize(100);
            // 日志实现（控制台输出 SQL）
            configuration.setLogImpl(StdOutImpl.class);
        };
    }

    /**
     * MyBatis-Plus 全局配置
     * <p>
     * 配置主键生成策略、表名映射、逻辑删除等全局属性。
     *
     * @return GlobalConfig 实例
     */
    @Bean
    public GlobalConfig globalConfig() {
        // 创建全局配置对象
        GlobalConfig globalConfig = new GlobalConfig();
        // 不打印 Banner（启动日志更简洁）
        globalConfig.setBanner(false);

        // 数据库相关配置
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        // 主键策略：自增（配合数据库 AUTO_INCREMENT）
        dbConfig.setIdType(IdType.AUTO);
        // 表名下划线转驼峰
        dbConfig.setTableUnderline(true);
        // 逻辑删除字段名（与实体类中 @TableLogic 注解的字段名对应）
        dbConfig.setLogicDeleteField(MyBaseEntity.Fields.deleted);
        // 逻辑删除的值（删除时将此字段设为 1）
        dbConfig.setLogicDeleteValue("1");
        // 逻辑未删除的值（正常数据此字段为 0）
        dbConfig.setLogicNotDeleteValue("0");

        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }
}