package com.xiyao.crypto.filter.wrapper;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.RandomUtil;
import com.xiyao.crypto.utils.EncryptUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 加密响应包装器
 * <p>
 * 对 HTTP 响应体进行加密处理，使用 SM2 + SM4 混合加密模式。
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>捕获业务代码写入的所有响应内容（通过 ByteArrayOutputStream）</li>
 *     <li>生成随机的 SM4 密钥（sm4Key）</li>
 *     <li>使用 SM2 公钥加密 sm4Key，将密文放入响应头</li>
 *     <li>使用 sm4Key 加密响应体明文</li>
 *     <li>输出加密后的响应内容和响应头</li>
 * </ol>
 * <p>
 * <b>加密流程：</b>
 * <pre>
 * 业务代码:
 *   1. 执行业务逻辑，生成 JSON 响应
 *   2. 将响应写入 response（被 EncryptResponseWrapper 拦截）
 *
 * EncryptorFilter:
 *   3. 调用 encryptContent() 方法
 *   4. 生成随机 SM4 密钥 (sm4Key)
 *   5. 用 SM2 公钥加密 sm4Key → encryptKey，放入响应头
 *   6. 获取响应内容明文，用 sm4Key 加密 → encryptContent
 *   7. 将 encryptContent 写入真正的 response
 *
 * 客户端:
 *   8. 从响应头获取 encryptKey
 *   9. 用 SM2 私钥解密得到 sm4Key
 *  10. 用 sm4Key 解密响应体得到明文
 * </pre>
 * <p>
 * <b>响应头说明：</b>
 * <ul>
 *     <li>Access-Control-Expose-Headers：暴露自定义响应头，使前端可以读取</li>
 *     <li>Access-Control-Allow-Origin：允许跨域访问</li>
 *     <li>headerFlag：存放加密的 SM4 密钥</li>
 * </ul>
 *
 * @author xiyao
 * @see DecryptRequestWrapper
 * @see EncryptUtils
 */
public class EncryptResponseWrapper extends HttpServletResponseWrapper {

    /**
     * 字节数组输出流，捕获所有写入的响应内容
     * <p>
     * 业务代码写入的响应内容首先写入此流，而不是直接发送到客户端。
     * 待业务处理完成后，再统一进行加密处理。
     */
    private final ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Servlet 输出流代理
     * <p>
     * 代理原始的 ServletOutputStream，将所有写入操作同时写入 byteArrayOutputStream。
     */
    private final ServletOutputStream servletOutputStream;

    /**
     * 字符输出流代理
     * <p>
     * 代理原始的 PrintWriter，将所有写入操作同时写入 byteArrayOutputStream。
     */
    private final PrintWriter printWriter;

    /**
     * 构造方法
     * <p>
     * 初始化输出流和代理对象，准备捕获响应内容。
     *
     * @param response 原始 HTTP 响应
     * @throws IOException 如果创建输出流失败
     */
    public EncryptResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);

        // 初始化字节数组输出流，用于捕获响应内容
        this.byteArrayOutputStream = new ByteArrayOutputStream();

        // 获取原始输出流并创建代理
        this.servletOutputStream = this.getOutputStream();

        // 创建字符输出流代理，写入内容同时进入 byteArrayOutputStream
        this.printWriter = new PrintWriter(byteArrayOutputStream);
    }

    /**
     * 获取字符输出流
     * <p>
     * 返回 PrintWriter 代理实例，调用者可以使用字符流方式写入响应。
     *
     * @return PrintWriter 字符输出流
     */
    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    /**
     * 刷新缓冲区
     * <p>
     * 将代理流中的数据刷新到底层 byteArrayOutputStream。
     *
     * @throws IOException 如果刷新失败
     */
    @Override
    public void flushBuffer() throws IOException {
        // 刷新字节流代理
        if (ObjectUtil.isNotNull(servletOutputStream)) {
            servletOutputStream.flush();
        }
        // 刷新字符流代理
        if (ObjectUtil.isNotNull(printWriter)) {
            printWriter.flush();
        }
    }

    /**
     * 重置响应内容
     * <p>
     * 清空 byteArrayOutputStream 中的内容，同时重置原始响应。
     */
    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    /**
     * 获取响应数据的字节数组
     * <p>
     * 在加密前获取捕获的响应内容。
     *
     * @return 响应内容的字节数组
     * @throws IOException 如果获取失败
     */
    public byte[] getResponseData() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 获取响应内容的字符串形式
     * <p>
     * 在加密前获取捕获的响应内容（UTF-8 编码）。
     *
     * @return 响应内容的字符串
     * @throws IOException 如果获取失败
     */
    public String getContent() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toString();
    }

    /**
     * 获取输出流（代理实现）
     * <p>
     * 返回一个自定义的 ServletOutputStream，将所有写入操作代理到 byteArrayOutputStream。
     *
     * @return ServletOutputStream 代理输出流
     * @throws IOException 如果创建失败
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // 异步写入回调，暂未实现
            }

            @Override
            public void write(int b) throws IOException {
                // 写入单个字节到捕获流
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                // 写入字节数组到捕获流
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                // 写入部分字节数组到捕获流
                byteArrayOutputStream.write(b, off, len);
            }
        };
    }

    /**
     * 加密并发送响应内容
     * <p>
     * 将捕获的响应内容进行 SM2 + SM4 混合加密，然后写入真实响应。
     * <p>
     * <b>加密步骤：</b>
     * <ol>
     *     <li>生成 16 位随机字符串作为 SM4 密钥</li>
     *     <li>使用 SM2 公钥加密 SM4 密钥，存入响应头</li>
     *     <li>使用 SM4 密钥加密响应体明文</li>
     *     <li>设置 CORS 响应头</li>
     *     <li>发送加密后的响应内容</li>
     * </ol>
     *
     * @param response   原始 HTTP 响应，用于写入加密内容和响应头
     * @param publicKey  SM2 公钥，用于加密 SM4 密钥
     * @param headerFlag 响应头标识，用于存储加密的 SM4 密钥
     * @throws IOException 如果加密或写入失败
     */
    public void encryptContent(HttpServletResponse response, String publicKey, String headerFlag) throws IOException {
        // 第1步：生成随机 SM4 密钥（16字符 = 128位）
        String sm4Key = RandomUtil.randomString(16);

        // 第2步：使用 SM2 公钥加密 SM4 密钥
        String encryptKey = EncryptUtils.encryptBySm2Hex(sm4Key, publicKey);

        // 第3步：设置响应头，允许前端访问加密密钥
        // 暴露自定义响应头，使前端 JavaScript 可以读取
        response.addHeader("Access-Control-Expose-Headers", headerFlag);
        // 允许跨域访问
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        // 将加密的 SM4 密钥存入响应头
        response.setHeader(headerFlag, encryptKey);

        // 设置响应编码为 UTF-8
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 第4步：获取接口响应内容（业务代码写入的 JSON）
        String content = this.getContent();

        // 第5步：使用 SM4 密钥加密响应内容（HEX 编码）
        String encryptContent = EncryptUtils.encryptBySm4Hex(content, sm4Key);

        // 第6步：发送加密后的响应内容
        response.getWriter().write(encryptContent);
    }
}