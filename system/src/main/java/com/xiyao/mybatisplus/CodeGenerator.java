package com.xiyao.mybatisplus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import com.xiyao.framework.base.BaseController;
import com.xiyao.mybatisplus.base.mapper.MyBaseMapper;
import com.xiyao.mybatisplus.base.service.MyBaseService;
import com.xiyao.mybatisplus.base.service.impl.MyBaseServiceImpl;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class CodeGenerator {

    public static final String URL = "jdbc:mysql://115.159.42.54:3306/dayplan?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&cachePrepStmts=true&rewriteBatchedStatements=true";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "tR5sW8yB3eH6qM9c";
    public static final String AUTHOR = "xiyao";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    private final static String PROJECT_PATH = System.getProperty("user.dir");
    public static final String FILE_PATH = "/system/src/main/java";
    public static final String PACKAGE = "com.xiyao.system";

    public static void main(String[] args) {
        generator();
    }

    public static void generator() {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> builder
                        .disableOpenDir() // 禁止打开输出目录
                        .outputDir(PROJECT_PATH + FILE_PATH) // 设置输出目录
                        .author(AUTHOR) // 设置作者名
                        .dateType(DateType.TIME_PACK) // 设置时间类型策略
                        .commentDate(YYYY_MM_DD) // 设置注释日期格式
                )
                .packageConfig(builder -> builder
                        .parent(PACKAGE).entity("entity").service("service").serviceImpl("service.impl").mapper("mapper").xml("mapper.xml").controller("controller")
                )
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        // 父类控制器 -> 开启驼峰转连字符 -> 开启生成@RestController控制器 -> 格式化文件名称 -> 覆盖已有文件
                        .controllerBuilder().superClass(BaseController.class).enableHyphenStyle().enableRestStyle().formatFileName("%sController").enableFileOverride()
                        // 禁用生成serialVersionUID -> 开启链式模型 -> 开启lombok模型
                        .entityBuilder().disableSerialVersionUID().enableChainModel().enableLombok(new ClassAnnotationAttributes(Data.class))
                        // 开启生成实体时生成字段注解 -> 设置乐观锁数据库表字段名称 -> 逻辑删除数据库字段名称 -> 数据库表字段映射到实体的命名策略
                        .enableTableFieldAnnotation().versionColumnName("version").logicDeleteColumnName("deleted").columnNaming(NamingStrategy.underline_to_camel)
                        // 添加父类公共字段 -> 自定义继承的Entity类全称 -> 格式化文件名称
                        // .addSuperEntityColumns("id", "remark", "create_time", "update_time", "delete_time", "deleted", "version").superClass(MyBaseEntity.class).formatFileName("%sEntity")
                        // 添加表字段填充
                        .addTableFills(new Column("create_time", FieldFill.INSERT), new Column("update_time", FieldFill.INSERT_UPDATE))
                        // 指定生成的主键的ID类型 -> 覆盖已有文件 -> 设置字段是否生成文档注释
                        .idType(IdType.AUTO).enableFileOverride().fieldUseJavaDoc(true)
                        // 父类Mapper -> 开启@Mapper注解 -> 格式化Mapper文件名称 -> 格式化Xml文件名称 -> 覆盖已有文件
                        .mapperBuilder().superClass(MyBaseMapper.class).enableMapperAnnotation()
                        .formatMapperFileName("%sMapper").formatXmlFileName("%sMapper").enableFileOverride()
                        // 父类Service -> 父类ServiceImpl -> 格式化Service文件名称 -> 格式化ServiceImpl文件名称 -> 覆盖已有文件
                        .serviceBuilder().superServiceClass(MyBaseService.class).superServiceImplClass(MyBaseServiceImpl.class)
                        .formatServiceFileName("%sService").formatServiceImplFileName("%sServiceImpl").enableFileOverride()
                )
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}