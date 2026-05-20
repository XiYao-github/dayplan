package com.xiyao.common.base.event;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.xiyao.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求事件基类 自动从 HttpServletRequest 中提取常用信息
 */
@Data
@Accessors(chain = true)
public abstract class MyBaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public MyBaseEvent() {
        HttpServletRequest request = WebUtils.getRequest();
        if (request != null) {
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

            // 解析 User-Agent
            if (StrUtil.isNotBlank(this.userAgent)) {
                UserAgent ua = UserAgentUtil.parse(this.userAgent);
                // 操作系统
                this.os = ua.getOs() != null ? ua.getOs().getName() : "未知";
                // 浏览器 + 版本
                String browserName = ua.getBrowser() != null ? ua.getBrowser().getName() : "未知";
                String browserVersion = ua.getVersion() != null ? " " + ua.getVersion() : "";
                this.browser = browserName + browserVersion;
                // 平台
                this.platform = ua.getPlatform() != null ? ua.getPlatform().getName() : "PC";
            }
        }
    }

    // ==================== 网络信息 ====================

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 客户端端口
     */
    private Integer clientPort;

    /**
     * 服务器IP:端口
     */
    private String serverIp;

    // ==================== 请求行信息 ====================

    /**
     * 请求方法 GET/POST/PUT/DELETE
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 查询参数
     */
    private String queryString;

    /**
     * 完整请求URL（含参数）
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
     * 来源页面
     */
    private String referer;

    /**
     * 跨域来源
     */
    private String origin;

    /**
     * 内容类型
     */
    private String contentType;

    // ==================== 设备信息（从User-Agent解析） ====================

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器（含版本）
     */
    private String browser;

    /**
     * 设备类型 PC/Mobile/Tablet
     */
    private String platform;
}