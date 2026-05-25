package com.xiyao.encrypt.filter.wrapper;

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
 * <p>
 * 对加密的 HTTP 请求进行解密处理，将加密的请求体转换为明文供业务代码使用。
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *     <li>从请求头获取 SM4 密钥的 SM2 密文（encryptKey）</li>
 *     <li>使用 SM2 私钥解密出 SM4 密钥（sm4Key）</li>
 *     <li>读取请求体密文，使用 SM4 密钥解密得到明文（decryptBody）</li>
 *     <li>将解密后的明文包装为新的请求体，供后续业务使用</li>
 * </ol>
 * <p>
 * <b>加密流程：</b>
 * <pre>
 * 客户端:
 *   1. 生成随机 SM4 密钥 (sm4Key)
 *   2. 用 SM2 公钥加密 sm4Key → encryptKey
 *   3. 用 sm4Key 加密请求体 → encryptBody
 *   4. 发送请求头 (headerFlag: encryptKey) 和请求体 (encryptBody)
 *
 * 服务端 EncryptorFilter:
 *   5. 将请求包装为 DecryptRequestWrapper
 *   6. 从请求头获取 encryptKey，用 SM2 私钥解密得到 sm4Key
 *   7. 用 sm4Key 解密请求体得到明文
 *   8. 后续业务处理使用明文
 * </pre>
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>与 EncryptorFilter 配合使用，对加密请求进行透明解密</li>
 *     <li>业务 Controller 可以直接获取明文请求，无需关心加解密细节</li>
 * </ul>
 *
 * @author xiyao
 * @see EncryptResponseWrapper
 * @see EncryptUtils
 */
public class DecryptRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 解密后的请求体字节数组
     * <p>
     * 存储解密后的明文请求体，供后续多次读取使用。
     * 使用字节数组可以支持多次读取 getInputStream()。
     */
    private final byte[] body;

    /**
     * 构造方法
     * <p>
     * 对请求进行解密处理，从请求头获取加密的 SM4 密钥，从请求体获取加密的业务数据，
     * 分别用 SM2 私钥和 SM4 密钥进行解密，得到明文请求体。
     *
     * @param request    原始 HTTP 请求
     * @param privateKey SM2 私钥，用于解密 SM4 密钥
     * @param headerFlag 请求头标识，用于获取加密的 SM4 密钥
     * @throws IOException 如果读取请求体或解密失败
     */
    public DecryptRequestWrapper(HttpServletRequest request, String privateKey, String headerFlag) throws IOException {
        super(request);

        // 第1步：从请求头获取 SM4 密钥的 SM2 密文
        String encryptKey = request.getHeader(headerFlag);

        // 第2步：使用 SM2 私钥解密 SM4 密钥
        String sm4Key = EncryptUtils.decryptBySm2(encryptKey, privateKey);

        // 第3步：读取请求体密文
        String requestBody = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);

        // 第4步：使用 SM4 密钥解密请求体明文
        String decryptBody = EncryptUtils.decryptBySm4(requestBody, sm4Key);

        // 存储解密后的明文，转换为字节数组供后续使用
        body = decryptBody.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取字符输入流
     * <p>
     * 返回解密后请求体的字符输入流，用于 getReader() 方法。
     *
     * @return BufferedReader 字符输入流
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 获取请求体长度
     *
     * @return 解密后请求体的字节长度
     */
    @Override
    public int getContentLength() {
        return body.length;
    }

    /**
     * 获取请求体长度（长整型）
     *
     * @return 解密后请求体的字节长度
     */
    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    /**
     * 获取请求内容类型
     * <p>
     * 统一返回 JSON 类型，因为加密请求通常是 JSON 格式。
     *
     * @return application/json
     */
    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    /**
     * 获取解密后的请求体输入流
     * <p>
     * 返回解密后明文请求体的二进制输入流，业务代码可以通过此流读取明文数据。
     *
     * @return ServletInputStream 解密后的请求体输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        // 将字节数组包装为 ByteArrayInputStream
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        // 创建 ServletInputStream 实现
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
                // 异步读取回调，暂未实现
            }
        };
    }
}