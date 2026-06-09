package com.xiyao.common.constant;

/**
 * 全局常量接口
 * <p>
 * 定义项目中使用的通用常量，
 * 包括日期格式、字符集、登录相关常量等。
 *
 * @author xiyao
 */
public interface Constant {

    // ==================== 日期时间格式 ====================

    /**
     * 日期时间格式（前端常用格式）
     */
    String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    String PATTERN_DATE = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    String PATTERN_TIME = "HH:mm:ss";

    /**
     * 默认时区（北京时间）
     */
    String TIME_ZONE = "GMT+8";

    // ==================== 字符集 ====================

    /**
     * UTF-8 字符集
     */
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    String GBK = "GBK";

    // ==================== URL 相关 ====================

    /**
     * www 主域前缀
     */
    String WWW = "www.";

    /**
     * HTTP 协议前缀
     */
    String HTTP = "http://";

    /**
     * HTTPS 协议前缀
     */
    String HTTPS = "https://";

    // ==================== 登录相关操作 ====================

    /**
     * 登录操作标识
     */
    String LOGIN = "login";

    /**
     * 注销操作标识
     */
    String LOGOUT = "logout";

    /**
     * 注册操作标识
     */
    String REGISTER = "register";

    // ==================== 数据库 ====================

    /**
     * 升序
     */
    String ASC = "asc";

    /**
     * 降序
     */
    String DESC = "desc";

    /**
     * 当前页码
     */
    String PAGE = "page";

    /**
     * 每页显示记录数
     */
    String LIMIT = "limit";

    /**
     * 排序字段
     */
    String ORDER_FIELD = "orderField";

    /**
     * 排序方式
     */
    String ORDER = "order";
}