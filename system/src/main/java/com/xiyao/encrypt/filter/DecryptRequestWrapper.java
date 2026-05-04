package com.xiyao.encrypt.filter;

import cn.hutool.core.io.IoUtil;
import com.xiyao.encrypt.utils.EncryptUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 解密请求包装器
 */
public class DecryptRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public DecryptRequestWrapper(HttpServletRequest request, String privateKey, String headerFlag) throws IOException {
        super(request);
        // 获取 sm4Key 密文
        String encryptKey = request.getHeader(headerFlag);
        // 使用 sm2 解密 sm4Key 密文
        String sm4Key = EncryptUtils.decryptBySm2(encryptKey, privateKey);
        // 获取 body 密文
        String requestBody = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
        // 使用 sm4 解密 body 密文
        String decryptBody = EncryptUtils.decryptBySm4(requestBody, sm4Key);
        body = decryptBody.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public int available() {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
