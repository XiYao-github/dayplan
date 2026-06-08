package com.xiyao.common.base.event;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.xiyao.framework.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求事件基类
 * <p>
 * 自动从 HttpServletRequest 中提取常用信息，
 * 包括客户端 IP、请求行信息、请求头信息、设备信息等。
 * 所有自定义事件类应继承此类。
 *
 * <p>
 * <b>自动采集的信息：</b>
 * <ul>
 *     <li>网络信息：clientIp、clientPort、serverIp</li>
 *     <li>请求行：requestMethod、requestUrl、queryString</li>
 *     <li>请求头：userAgent、referer、origin、contentType</li>
 *     <li>设备信息：os、browser、platform（通过 User-Agent 解析）</li>
 * </ul>
 *
 * @author xiyao
 */
@Data
@Accessors(chain = true)
public abstract class MyBaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     * <p>
     * 在构造时自动从当前请求上下文中提取信息。
     * 如果不在 Web 请求上下文中，所有字段将为 null。
     */
    public MyBaseEvent() {
        HttpServletRequest request = WebUtils.getRequest();
        if (ObjectUtil.isNotNull(request)) {
            // 网络信息
            this.clientIp = JakartaServletUtil.getClientIP(request);
            this.clientPort = request.getRemotePort();
            this.serverIp = request.getServerName() + ":" + request.getServerPort();

            // 请求行信息
            this.requestMethod = request.getMethod();
            this.requestUrl = request.getRequestURL().toString();
            this.queryString = request.getQueryString();

            // 请求头信息
            this.userAgent = request.getHeader("User-Agent");
            this.referer = request.getHeader("Referer");
            this.origin = request.getHeader("Origin");
            this.contentType = request.getContentType();
            this.traceId = MDC.get("traceId");

            // 解析 User-Agent
            if (StrUtil.isNotBlank(this.userAgent)) {
                UserAgent ua = UserAgentUtil.parse(this.userAgent);
                // 操作系统
                this.os = ObjectUtil.isNotNull(ua.getOs()) ? ua.getOs().getName() : "未知";
                // 浏览器 + 版本
                String browserName = ObjectUtil.isNotNull(ua.getBrowser()) ? ua.getBrowser().getName() : "未知";
                String browserVersion = ObjectUtil.isNotNull(ua.getVersion()) ? " " + ua.getVersion() : "";
                this.browser = browserName + browserVersion;
                // 平台
                this.platform = ObjectUtil.isNotNull(ua.getPlatform()) ? ua.getPlatform().getName() : "PC";
            }
        }
    }

    // ==================== 网络信息 ====================

    /**
     * 客户端 IP 地址
     */
    private String clientIp;

    /**
     * 客户端端口号
     */
    private Integer clientPort;

    /**
     * 服务器 IP 和端口
     */
    private String serverIp;

    // ==================== 请求行信息 ====================

    /**
     * HTTP 请求方法（GET、POST、PUT、DELETE 等）
     */
    private String requestMethod;

    /**
     * 请求 URI（不包含域名和端口）
     */
    private String requestUrl;

    /**
     * 查询参数字符串
     */
    private String queryString;

    /**
     * 获取完整的请求 URL（含查询参数）
     *
     * @return 完整 URL，如 http://localhost:8080/api/user?id=1
     */
    public String getFullUrl() {
        if (StrUtil.isBlank(queryString)) {
            return requestUrl;
        }
        return requestUrl + "?" + queryString;
    }

    // ==================== 请求头信息 ====================

    /**
     * User-Agent 原始字符串
     */
    private String userAgent;

    /**
     * 来源页面（Referer 头）
     */
    private String referer;

    /**
     * 跨域来源（Origin 头）
     */
    private String origin;

    /**
     * 内容类型（Content-Type 头）
     */
    private String contentType;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    // ==================== 设备信息（从 User-Agent 解析） ====================

    /**
     * 操作系统名称
     */
    private String os;

    /**
     * 浏览器名称和版本
     */
    private String browser;

    /**
     * 平台类型（PC/Mobile/Tablet）
     */
    private String platform;
}