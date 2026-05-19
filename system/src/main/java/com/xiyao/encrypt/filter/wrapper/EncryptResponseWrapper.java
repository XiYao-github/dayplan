package com.xiyao.encrypt.filter.wrapper;

import cn.hutool.core.util.RandomUtil;
import com.xiyao.encrypt.utils.EncryptUtils;
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
 */
public class EncryptResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter printWriter;

    public EncryptResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.servletOutputStream = this.getOutputStream();
        this.printWriter = new PrintWriter(byteArrayOutputStream);
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    public byte[] getResponseData() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toByteArray();
    }

    public String getContent() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toString();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byteArrayOutputStream.write(b, off, len);
            }
        };
    }

    /**
     * (包装器 -> 加密 -> 响应)
     *
     * @param response   原始响应
     * @param publicKey  响应加密公钥
     * @param headerFlag 加密头标识
     */
    public void encryptContent(HttpServletResponse response, String publicKey, String headerFlag) throws IOException {
        // 生成 sm4Key
        String sm4Key = RandomUtil.randomString(16);
        // 使用 sm2 加密 sm4Key
        String encryptKey = EncryptUtils.encryptBySm2Hex(sm4Key, publicKey);
        // 设置响应头，vue版本需要设置
        response.addHeader("Access-Control-Expose-Headers", headerFlag);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader(headerFlag, encryptKey);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 获取接口响应内容
        String content = this.getContent();
        // 使用 sm4 加密 content 明文
        String encryptContent = EncryptUtils.encryptBySm4Hex(content, sm4Key);
        // 响应加密内容
        response.getWriter().write(encryptContent);
    }
}
