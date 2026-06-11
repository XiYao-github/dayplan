package com.xiyao.common.constant;

/**
 * 全局常量接口
 * <p>
 * 定义项目中使用的通用常量，包括日期格式、字符集、排序方向、数据库相关常量等。
 * 通过接口 + 常量的方式组织，便于集中管理和使用。
 *
 * @author xiyao
 */
public interface Constant {

    // ==================== 日期时间格式常量 ====================

    /**
     * 日期时间格式（前端常用格式）
     * <p>
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     * <p>
     * 格式：yyyy-MM-dd
     */
    String PATTERN_DATE = "yyyy-MM-dd";

    /**
     * 时间格式
     * <p>
     * 格式：HH:mm:ss
     */
    String PATTERN_TIME = "HH:mm:ss";

    /**
     * 默认时区（北京时间）
     */
    String TIME_ZONE = "GMT+8";

    // ==================== 字符集常量 ====================

    /**
     * UTF-8 字符集
     */
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    String GBK = "GBK";

    // ==================== URL 相关常量 ====================

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

    // ==================== 登录操作常量 ====================

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

    // ==================== 数据库相关常量 ====================

    /**
     * 当前记录起始索引（分页参数）
     */
    String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数（分页参数）
     */
    String PAGE_SIZE = "pageSize";

    /**
     * 排序列（分页参数）
     */
    String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序方式（分页参数）
     */
    String ORDER = "order";

    /**
     * 升序
     */
    String ASC = "asc";

    /**
     * 降序
     */
    String DESC = "desc";

    /**
     * 排序方向参数名
     */
    String IS_ASC = "isAsc";
}