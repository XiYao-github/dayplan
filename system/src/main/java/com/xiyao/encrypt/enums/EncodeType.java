package com.xiyao.encrypt.enums;

/**
 * 编码类型枚举
 * <p>
 * 定义加密后数据的编码格式，支持 BASE64 和 HEX（十六进制）两种编码方式。
 * <p>
 * <b>编码对比：</b>
 * <table border="1">
 *     <tr><th>编码</th><th>特点</th><th>适用场景</th></tr>
 *     <tr><td>BASE64</td><td>字符集更小，体积约为原长度/3</td><td>JSON 传输、URL 参数</td></tr>
 *     <tr><td>HEX</td><td>人类可读，体积约为原长度*2</td><td>日志输出、调试</td></tr>
 * </table>
 *
 * @author xiyao
 */
public enum EncodeType {

    /**
     * 默认编码
     * <p>
     * 使用 yml 配置文件中的全局编码配置。
     */
    DEFAULT,

    /**
     * BASE64 编码
     * <p>
     * 使用 Base64 算法对加密后的字节数据进行编码，输出字符集为 A-Z、a-z、0-9、+、/。
     * 特点是数据更紧凑，适合在 JSON 中传输。
     */
    BASE64,

    /**
     * HEX 十六进制编码
     * <p>
     * 将加密后的每个字节转换为两个十六进制字符（0-9、A-F）。
     * 特点是人类可读，方便调试和日志输出。
     */
    HEX;

}