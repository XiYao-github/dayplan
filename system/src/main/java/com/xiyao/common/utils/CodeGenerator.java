package com.xiyao.common.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import com.xiyao.common.base.controller.MyBaseController;
import com.xiyao.common.base.mapper.MyBaseMapper;
import com.xiyao.common.base.service.MyBaseService;
import com.xiyao.common.base.service.impl.MyBaseServiceImpl;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MyBatis-Plus 代码生成器
 * <p>
 * 根据数据库表自动生成 Entity、Mapper、Service、Controller。
 * 生成的文件继承项目自定义的基类，使用 Lombok 注解。
 *
 * <p>
 * <b>使用方式：</b>
 * 直接运行 main 方法，控制台输入表名即可。
 * 支持一次输入多个表，逗号分隔。
 * 输入 all 生成所有表。
 *
 * <p>
 * <b>生成的文件：</b>
 * <ul>
 *     <li>entity：继承 MyBaseEntity，包含通用审计字段</li>
 *     <li>mapper：继承 MyBaseMapper，使用 @Mapper 注解</li>
 *     <li>service：继承 MyBaseService</li>
 *     <li>serviceImpl：继承 MyBaseServiceImpl</li>
 *     <li>controller：继承 MyBaseController，使用 @RestController</li>
 * </ul>
 *
 * @author xiyao
 */
public class CodeGenerator {

    /**
     * 数据库 URL
     */
    public static final String URL = "jdbc:mysql://115.159.42.54:3306/dayplan?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&cachePrepStmts=true&rewriteBatchedStatements=true";
    /**
     * 数据库用户名
     */
    public static final String USERNAME = "root";
    /**
     * 数据库密码（请修改为实际密码）
     */
    public static final String PASSWORD = "tR5sW8yB3eH6qM9c";
    /**
     * 作者名
     */
    public static final String AUTHOR = "xiyao";
    /**
     * 日期格式
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 项目根路径
     */
    private final static String PROJECT_PATH = System.getProperty("user.dir");
    /**
     * 代码输出路径
     */
    public static final String FILE_PATH = "/system/src/main/java";
    /**
     * 包名
     */
    public static final String PACKAGE = "com.xiyao.system";

    /**
     * 主方法，运行代码生成器
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        generator();
    }

    /**
     * 执行代码生成
     * <p>
     * 配置生成策略并执行生成。
     */
    public static void generator() {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> builder
                        .disableOpenDir()
                        .outputDir(PROJECT_PATH + FILE_PATH)
                        .author(AUTHOR)
                        .dateType(DateType.TIME_PACK)
                        .commentDate(YYYY_MM_DD)
                )
                .packageConfig(builder -> builder
                        .parent(PACKAGE).entity("entity").service("service").serviceImpl("service.impl").mapper("mapper").xml("mapper.xml").controller("controller")
                )
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .controllerBuilder().superClass(MyBaseController.class).enableHyphenStyle().enableRestStyle().formatFileName("%sController").enableFileOverride()
                        .entityBuilder().disableSerialVersionUID().enableChainModel().enableLombok(new ClassAnnotationAttributes(Data.class))
                        .enableTableFieldAnnotation().versionColumnName("version").logicDeleteColumnName("deleted").columnNaming(NamingStrategy.underline_to_camel)
                        .addTableFills(new Column("create_time", FieldFill.INSERT), new Column("update_time", FieldFill.INSERT_UPDATE))
                        .idType(IdType.AUTO).enableFileOverride().fieldUseJavaDoc(true)
                        .mapperBuilder().superClass(MyBaseMapper.class).mapperAnnotation(org.apache.ibatis.annotations.Mapper.class)
                        .formatMapperFileName("%sMapper").formatXmlFileName("%sMapper").enableFileOverride()
                        .serviceBuilder().superServiceClass(MyBaseService.class).superServiceImplClass(MyBaseServiceImpl.class)
                        .formatServiceFileName("%sService").formatServiceImplFileName("%sServiceImpl").enableFileOverride()
                )
                .execute();
    }

    /**
     * 处理表名列表
     * <p>
     * 输入 all 返回空列表（生成所有表），
     * 否则按逗号分隔为列表。
     *
     * @param tables 用户输入的表名字符串
     * @return 表名列表
     */
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}