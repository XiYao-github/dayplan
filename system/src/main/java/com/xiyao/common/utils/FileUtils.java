package com.xiyao.common.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储工具类
 * <p>
 * 支持本地存储、云存储（阿里云OSS、腾讯云COS、七牛云）等多种存储方式。
 * 通过配置切换不同的存储策略。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>文件上传：支持本地和云存储</li>
 *     <li>文件下载：支持本地文件和云端文件</li>
 *     <li>文件删除：支持删除本地和云端文件</li>
 *     <li>URL 生成：获取文件的访问地址</li>
 * </ul>
 *
 * <p>
 * <b>配置项：</b>
 * <pre>{@code
 * file-storage:
 *   type: local  # local/aliyun/tencent/qiniu
 *   local:
 *     basePath: /data/uploads
 *     domain: http://localhost:8080/uploads
 *   aliyun:
 *     endpoint: oss-cn-shanghai.aliyuncs.com
 *     accessKeyId: xxx
 *     accessKeySecret: xxx
 *     bucketName: dayplan
 *     domain: https://dayplan.oss-cn-shanghai.aliyuncs.com
 * }</pre>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 上传文件
 * String url = FileUtils.upload(multipartFile, "avatar");
 *
 * // 上传到指定路径
 * String url = FileUtils.upload(multipartFile, "avatar", "user/123");
 *
 * // 下载文件
 * byte[] data = FileUtils.download(url);
 *
 * // 删除文件
 * FileUtils.delete(url);
 * }</pre>
 *
 * @author xiyao
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    /**
     * 默认存储类型（本地存储）
     */
    private static String STORAGE_TYPE = "local";

    /**
     * 本地存储配置
     */
    private static LocalConfig LOCAL_CONFIG = new LocalConfig();

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 文件名分隔符
     */
    private static final String FILE_SEPARATOR = "_";

    /**
     * 设置存储类型
     *
     * @param type 存储类型：local/aliyun/tencent/qiniu
     */
    public static void setStorageType(String type) {
        STORAGE_TYPE = type;
    }

    /**
     * 设置本地存储配置
     *
     * @param config 本地存储配置
     */
    public static void setLocalConfig(LocalConfig config) {
        LOCAL_CONFIG = config;
    }

    /**
     * 上传文件
     * <p>
     * 根据配置的存储类型，自动选择本地或云存储。
     *
     * @param file     上传的文件
     * @param basePath 基础路径，如 "avatar"、"doc"
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    public static String upload(MultipartFile file, String basePath) throws IOException {
        return upload(file, basePath, null);
    }

    /**
     * 上传文件
     *
     * @param file      上传的文件
     * @param basePath  基础路径
     * @param fileName  自定义文件名（不含扩展名），为 null 时自动生成
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    public static String upload(MultipartFile file, String basePath, String fileName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (ObjectUtil.isNull(fileName)) {
            fileName = generateFileName(extension);
        } else {
            fileName = fileName + extension;
        }

        // 按日期分目录
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String relativePath = basePath + "/" + datePath + "/" + fileName;

        // 根据存储类型上传
        switch (STORAGE_TYPE) {
            case "aliyun":
                return uploadToAliyun(file, relativePath);
            case "tencent":
                return uploadToTencent(file, relativePath);
            case "qiniu":
                return uploadToQiniu(file, relativePath);
            default:
                return uploadToLocal(file, relativePath);
        }
    }

    /**
     * 上传字节数据
     *
     * @param data      字节数据
     * @param fileName  文件名
     * @param basePath  基础路径
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    public static String upload(byte[] data, String fileName, String basePath) throws IOException {
        if (ArrayUtil.isEmpty(data)) {
            throw new IllegalArgumentException("数据不能为空");
        }

        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String relativePath = basePath + "/" + datePath + "/" + fileName;

        return uploadToLocal(data, relativePath);
    }

    /**
     * 上传到本地存储
     *
     * @param file         文件
     * @param relativePath 相对路径
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    private static String uploadToLocal(MultipartFile file, String relativePath) throws IOException {
        String basePath = LOCAL_CONFIG.getBasePath();
        Path targetPath = Paths.get(basePath, relativePath);

        // 创建父目录
        Files.createDirectories(targetPath.getParent());

        // 写入文件
        file.transferTo(targetPath.toFile());

        log.info("文件上传到本地: {}", relativePath);

        // 返回访问 URL
        return buildUrl(relativePath);
    }

    /**
     * 上传字节数据到本地
     *
     * @param data         字节数据
     * @param relativePath 相对路径
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    private static String uploadToLocal(byte[] data, String relativePath) throws IOException {
        String basePath = LOCAL_CONFIG.getBasePath();
        Path targetPath = Paths.get(basePath, relativePath);

        // 创建父目录
        Files.createDirectories(targetPath.getParent());

        // 写入文件
        Files.write(targetPath, data);

        log.info("数据上传到本地: {}", relativePath);

        return buildUrl(relativePath);
    }

    /**
     * 上传到阿里云 OSS（占位实现）
     */
    private static String uploadToAliyun(MultipartFile file, String relativePath) throws IOException {
        // TODO: 集成阿里云 OSS SDK
        // AliyunOSSUtils.upload(file.getBytes(), relativePath);
        log.warn("阿里云 OSS 暂未实现，使用本地存储");
        return uploadToLocal(file, relativePath);
    }

    /**
     * 上传到腾讯云 COS（占位实现）
     */
    private static String uploadToTencent(MultipartFile file, String relativePath) throws IOException {
        // TODO: 集成腾讯云 COS SDK
        // TencentCOSUtils.upload(file.getBytes(), relativePath);
        log.warn("腾讯云 COS 暂未实现，使用本地存储");
        return uploadToLocal(file, relativePath);
    }

    /**
     * 上传到七牛云（占位实现）
     */
    private static String uploadToQiniu(MultipartFile file, String relativePath) throws IOException {
        // TODO: 集成七牛云 SDK
        // QiniuUtils.upload(file.getBytes(), relativePath);
        log.warn("七牛云暂未实现，使用本地存储");
        return uploadToLocal(file, relativePath);
    }

    /**
     * 下载文件
     *
     * @param url 文件 URL 或相对路径
     * @return 字节数据
     * @throws IOException IO 异常
     */
    public static byte[] download(String url) throws IOException {
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 如果是完整 URL，提取路径部分
        String relativePath = extractPath(url);

        // 根据存储类型下载
        switch (STORAGE_TYPE) {
            case "aliyun":
                return downloadFromAliyun(relativePath);
            case "tencent":
                return downloadFromTencent(relativePath);
            case "qiniu":
                return downloadFromQiniu(relativePath);
            default:
                return downloadFromLocal(relativePath);
        }
    }

    /**
     * 从本地下载文件
     *
     * @param relativePath 相对路径
     * @return 字节数据
     * @throws IOException IO 异常
     */
    private static byte[] downloadFromLocal(String relativePath) throws IOException {
        Path filePath = Paths.get(LOCAL_CONFIG.getBasePath(), relativePath);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + relativePath);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * 从阿里云 OSS 下载（占位实现）
     */
    private static byte[] downloadFromAliyun(String relativePath) throws IOException {
        // TODO: 集成阿里云 OSS SDK
        log.warn("阿里云 OSS 下载暂未实现");
        return downloadFromLocal(relativePath);
    }

    /**
     * 从腾讯云 COS 下载（占位实现）
     */
    private static byte[] downloadFromTencent(String relativePath) throws IOException {
        // TODO: 集成腾讯云 COS SDK
        log.warn("腾讯云 COS 下载暂未实现");
        return downloadFromLocal(relativePath);
    }

    /**
     * 从七牛云下载（占位实现）
     */
    private static byte[] downloadFromQiniu(String relativePath) throws IOException {
        // TODO: 集成七牛云 SDK
        log.warn("七牛云下载暂未实现");
        return downloadFromLocal(relativePath);
    }

    /**
     * 删除文件
     *
     * @param url 文件 URL 或相对路径
     * @return 是否删除成功
     */
    public static boolean delete(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }

        String relativePath = extractPath(url);

        switch (STORAGE_TYPE) {
            case "aliyun":
                return deleteFromAliyun(relativePath);
            case "tencent":
                return deleteFromTencent(relativePath);
            case "qiniu":
                return deleteFromQiniu(relativePath);
            default:
                return deleteFromLocal(relativePath);
        }
    }

    /**
     * 从本地删除文件
     *
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    private static boolean deleteFromLocal(String relativePath) {
        try {
            Path filePath = Paths.get(LOCAL_CONFIG.getBasePath(), relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", relativePath, e);
            return false;
        }
    }

    /**
     * 从阿里云 OSS 删除（占位实现）
     */
    private static boolean deleteFromAliyun(String relativePath) {
        // TODO: 集成阿里云 OSS SDK
        log.warn("阿里云 OSS 删除暂未实现");
        return deleteFromLocal(relativePath);
    }

    /**
     * 从腾讯云 COS 删除（占位实现）
     */
    private static boolean deleteFromTencent(String relativePath) {
        // TODO: 集成腾讯云 COS SDK
        log.warn("腾讯云 COS 删除暂未实现");
        return deleteFromLocal(relativePath);
    }

    /**
     * 从七牛云删除（占位实现）
     */
    private static boolean deleteFromQiniu(String relativePath) {
        // TODO: 集成七牛云 SDK
        log.warn("七牛云删除暂未实现");
        return deleteFromLocal(relativePath);
    }

    /**
     * 检查文件是否存在
     *
     * @param url 文件 URL 或相对路径
     * @return 是否存在
     */
    public static boolean exists(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }

        String relativePath = extractPath(url);

        switch (STORAGE_TYPE) {
            case "aliyun":
                return existsOnAliyun(relativePath);
            case "tencent":
                return existsOnTencent(relativePath);
            case "qiniu":
                return existsOnQiniu(relativePath);
            default:
                return existsOnLocal(relativePath);
        }
    }

    /**
     * 检查本地文件是否存在
     *
     * @param relativePath 相对路径
     * @return 是否存在
     */
    private static boolean existsOnLocal(String relativePath) {
        Path filePath = Paths.get(LOCAL_CONFIG.getBasePath(), relativePath);
        return Files.exists(filePath);
    }

    /**
     * 检查阿里云 OSS 文件是否存在（占位实现）
     */
    private static boolean existsOnAliyun(String relativePath) {
        // TODO: 集成阿里云 OSS SDK
        return existsOnLocal(relativePath);
    }

    /**
     * 检查腾讯云 COS 文件是否存在（占位实现）
     */
    private static boolean existsOnTencent(String relativePath) {
        // TODO: 集成腾讯云 COS SDK
        return existsOnLocal(relativePath);
    }

    /**
     * 检查七牛云文件是否存在（占位实现）
     */
    private static boolean existsOnQiniu(String relativePath) {
        // TODO: 集成七牛云 SDK
        return existsOnLocal(relativePath);
    }

    /**
     * 获取文件访问 URL
     *
     * @param relativePath 相对路径
     * @return 完整的访问 URL
     */
    private static String buildUrl(String relativePath) {
        String domain = LOCAL_CONFIG.getDomain();
        if (StrUtil.isBlank(domain)) {
            return relativePath;
        }
        // 移除开头的斜杠
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return domain.endsWith("/") ? domain + relativePath : domain + "/" + relativePath;
    }

    /**
     * 从 URL 中提取相对路径
     *
     * @param url 文件 URL
     * @return 相对路径
     */
    private static String extractPath(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 提取域名后面的路径
            int idx = url.indexOf("/", 8);
            return idx > 0 ? url.substring(idx + 1) : "";
        }
        return url;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（含点），如 ".jpg"
     */
    private static String getExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    /**
     * 生成唯一的文件名
     *
     * @param extension 扩展名
     * @return 唯一的文件名
     */
    private static String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + FILE_SEPARATOR + System.currentTimeMillis() + extension;
    }

    /**
     * 下载日志文件（便捷方法）
     *
     * @param logFileName 日志文件名
     * @return 字节数据
     * @throws IOException IO 异常
     */
    public static byte[] downloadLog(String logFileName) throws IOException {
        String logPath = "logs/" + logFileName;
        return download(logPath);
    }

    /**
     * 上传日志文件（便捷方法）
     *
     * @param logData 日志数据
     * @param logFileName 日志文件名
     * @return 文件访问 URL
     * @throws IOException IO 异常
     */
    public static String uploadLog(byte[] logData, String logFileName) throws IOException {
        String relativePath = "logs/" + LocalDate.now().format(DATE_FORMATTER) + "/" + logFileName;
        return uploadToLocal(logData, relativePath);
    }

    /**
     * 本地存储配置
     */
    @Data
    public static class LocalConfig {
        /**
         * 存储根目录
         */
        private String basePath = "/data/uploads";

        /**
         * 访问域名
         */
        private String domain = "http://localhost:8080/uploads";
    }
}