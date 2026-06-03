package com.xiyao.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 工具类
 * <p>
 * 封装 Alibaba EasyExcel 库，提供便捷的 Excel 导入导出功能。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>Excel 导出：支持单个 sheet 和多 sheet 导出</li>
 *     <li>Excel 导入：支持指定行数分页读取</li>
 *     <li>模板填充：支持基于模板生成 Excel</li>
 *     <li>简洁 API：一行代码完成导入导出</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 导出（浏览器下载）
 * List<User> users = userService.list();
 * ExcelUtils.export(users, UserExcel.class, "用户列表", response);
 *
 * // 导入
 * List<User> users = ExcelUtils.importExcel(file.getInputStream(), UserExcel.class);
 *
 * // 模板填充导出
 * Map<String, Object> data = Map.of("name", "张三", "date", "2024-01-01");
 * ExcelUtils.fillTemplate(templatePath, data, "报告.xlsx", response);
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtils {

    /**
     * 默认 sheet 名称
     */
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    /**
     * 导出 Excel（简洁版）
     *
     * @param data     数据列表
     * @param clazz    Excel 映射类（使用 EasyExcel 注解）
     * @param fileName 文件名（不含扩展名）
     * @param response HTTP 响应
     * @param <T>      数据类型
     */
    public static <T> void export(List<T> data, Class<T> clazz, String fileName, HttpServletResponse response) {
        export(data, clazz, fileName, DEFAULT_SHEET_NAME, response);
    }

    /**
     * 导出 Excel
     *
     * @param data      数据列表
     * @param clazz     Excel 映射类
     * @param fileName  文件名（不含扩展名）
     * @param sheetName sheet 名称
     * @param response  HTTP 响应
     * @param <T>       数据类型
     */
    public static <T> void export(List<T> data, Class<T> clazz, String fileName, String sheetName, HttpServletResponse response) {
        try {
            setExcelResponse(response, fileName);
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("导出 Excel 失败", e);
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出 Excel 到文件
     *
     * @param data     数据列表
     * @param clazz    Excel 映射类
     * @param filePath 完整文件路径
     * @param <T>      数据类型
     */
    public static <T> void exportToFile(List<T> data, Class<T> clazz, String filePath) {
        try {
            EasyExcel.write(filePath, clazz)
                    .sheet(DEFAULT_SHEET_NAME)
                    .doWrite(data);
        } catch (Exception e) {
            log.error("导出 Excel 到文件失败: {}", filePath, e);
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（全部数据）
     *
     * @param inputStream 文件输入流
     * @param clazz       Excel 映射类
     * @param <T>         数据类型
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        try {
            return EasyExcel.read(inputStream, clazz, null)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("导入 Excel 失败", e);
            throw new RuntimeException("导入 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（分页读取，适用于大数据量）
     *
     * @param inputStream  文件输入流
     * @param clazz        Excel 映射类
     * @param pageSize     每页行数
     * @param pageListener 分页监听器
     * @param <T>          数据类型
     */
    public static <T> void importExcel(InputStream inputStream, Class<T> clazz, int pageSize, PageReadListener<T> pageListener) {
        EasyExcel.read(inputStream, clazz, pageListener)
                .sheet()
                .headRowNumber(pageSize)
                .doRead();
    }

    /**
     * 导入 Excel 文件
     *
     * @param file  上传的文件
     * @param clazz Excel 映射类
     * @param <T>   数据类型
     * @return 数据列表
     */
    public static <T> List<T> importExcel(File file, Class<T> clazz) {
        try {
            return importExcel(new FileInputStream(file), clazz);
        } catch (FileNotFoundException e) {
            log.error("文件不存在: {}", file.getPath(), e);
            throw new RuntimeException("文件不存在: " + file.getPath(), e);
        }
    }

    /**
     * 读取 Excel（同步读取所有数据）
     *
     * @param inputStream 文件输入流
     * @param clazz       Excel 映射类
     * @param sheetNo     sheet 编号（从 1 开始）
     * @param <T>         数据类型
     * @return 数据列表
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> clazz, int sheetNo) {
        try {
            return EasyExcel.read(inputStream, clazz, null)
                    .sheet(sheetNo)
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取 Excel 失败", e);
            throw new RuntimeException("读取 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取 Excel（指定 sheet 名称）
     *
     * @param inputStream 文件输入流
     * @param clazz       Excel 映射类
     * @param sheetName   sheet 名称
     * @param <T>         数据类型
     * @return 数据列表
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> clazz, String sheetName) {
        try {
            return EasyExcel.read(inputStream, clazz, null)
                    .sheet(sheetName)
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取 Excel 失败", e);
            throw new RuntimeException("读取 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 模板填充导出
     * <p>
     * 使用预先准备好的 Excel 模板，填充数据后导出。
     *
     * @param templatePath 模板文件路径（classpath 或文件路径）
     * @param data         填充数据（key 对应模板中的占位符）
     * @param fileName     导出文件名
     * @param response     HTTP 响应
     */
    public static void fillTemplate(String templatePath, Map<String, Object> data, String fileName, HttpServletResponse response) {
        try {
            setExcelResponse(response, fileName);

            // 获取模板输入流
            InputStream templateStream = getTemplateStream(templatePath);

            EasyExcel.write(response.getOutputStream())
                    .withTemplate(templateStream)
                    .sheet()
                    .doFill(data);
        } catch (IOException e) {
            log.error("模板填充导出失败", e);
            throw new RuntimeException("模板填充导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 模板填充导出（多数据源）
     *
     * @param templatePath 模板文件路径
     * @param dataList     数据列表（多个 key 对应多个数据区域）
     * @param fileName     导出文件名
     * @param response     HTTP 响应
     */
    public static void fillTemplateMultiple(String templatePath, Map<String, List<?>> dataList, String fileName, HttpServletResponse response) {
        try {
            setExcelResponse(response, fileName);

            InputStream templateStream = getTemplateStream(templatePath);

            // 创建工作簿
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .withTemplate(templateStream)
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet().build();

            // 填充多个数据区域
            for (Map.Entry<String, List<?>> entry : dataList.entrySet()) {
                FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
                excelWriter.fill(entry.getValue(), fillConfig, writeSheet);
            }

            excelWriter.finish();
        } catch (IOException e) {
            log.error("模板填充导出失败", e);
            throw new RuntimeException("模板填充导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取模板文件输入流
     *
     * @param templatePath 模板路径
     * @return 输入流
     * @throws IOException IO 异常
     */
    private static InputStream getTemplateStream(String templatePath) throws IOException {
        // 尝试从 classpath 加载
        InputStream stream = ExcelUtils.class.getClassLoader().getResourceAsStream(templatePath);
        if (stream != null) {
            return stream;
        }

        // 尝试作为文件路径加载
        File file = new File(templatePath);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        throw new FileNotFoundException("模板文件不存在: " + templatePath);
    }

    /**
     * 设置 Excel 响应头
     *
     * @param response HTTP 响应
     * @param fileName 文件名（不含扩展名）
     * @throws IOException IO 异常
     */
    private static void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 编码文件名
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }

    /**
     * 创建 ExcelWriter 用于多 sheet 导出
     *
     * @param response HTTP 响应
     * @param fileName 文件名
     * @return ExcelWriter
     */
    public static ExcelWriter createExcelWriter(HttpServletResponse response, String fileName) throws IOException {
        setExcelResponse(response, fileName);
        return EasyExcel.write(response.getOutputStream()).build();
    }

    /**
     * 获取写 sheet
     *
     * @param sheetNo   sheet 编号
     * @param sheetName sheet 名称
     * @return WriteSheet
     */
    public static WriteSheet getWriteSheet(int sheetNo, String sheetName) {
        return EasyExcel.writerSheet(sheetNo, sheetName).build();
    }

    /**
     * 获取写 sheet（使用默认名称）
     *
     * @param sheetNo sheet 编号
     * @return WriteSheet
     */
    public static WriteSheet getWriteSheet(int sheetNo) {
        return EasyExcel.writerSheet(sheetNo).build();
    }

    /**
     * 关闭 ExcelReader
     *
     * @param excelReader ExcelReader
     */
    public static void close(ExcelReader excelReader) {
        if (excelReader != null) {
            excelReader.finish();
        }
    }
}