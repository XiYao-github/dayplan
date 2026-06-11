package com.xiyao.common.utils.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 分页查询实体类
 * <p>
 * 用于封装分页查询的参数，包括分页大小、当前页码、排序字段和排序方向。
 * 支持多字段排序，白名单校验防止 SQL 注入。
 *
 * @author xiyao
 */
@Slf4j
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页数
     */
    private Integer pageNum;

    /**
     * 排序列（支持驼峰格式或数据库列名格式）
     */
    private String orderByColumn;

    /**
     * 排序方向（asc 或 desc，多个用逗号分隔）
     */
    private String isAsc;

    /**
     * 升序常量
     */
    public static final String ASC = "asc";

    /**
     * 降序常量
     */
    public static final String DESC = "desc";

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页记录数
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页记录数（防止恶意大分页）
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 多字段分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 合法的列名正则：字母、数字、下划线，多个列名用逗号分隔
     * 用于防止 SQL 注入，只允许安全的字符
     */
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*$");

    /**
     * 合法的排序方向正则：asc 或 desc，多个用逗号分隔（不区分大小写）
     */
    private static final Pattern DIRECTION_PATTERN = Pattern.compile("^(asc|desc)(,(asc|desc))*$", Pattern.CASE_INSENSITIVE);

    /**
     * 构造函数
     *
     * @param pageSize 每页记录数
     * @param pageNum 当前页码
     */
    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    /**
     * 构建 MyBatis-Plus 分页对象
     * <p>
     * 根据分页参数构建 Page 对象，自动处理以下边界情况：
     * <ul>
     *     <li>页码为空或小于等于0时，使用默认值 DEFAULT_PAGE_NUM</li>
     *     <li>每页记录数为空或小于等于0时，使用默认值 DEFAULT_PAGE_SIZE</li>
     *     <li>每页记录数超过最大限制时，使用 MAX_PAGE_SIZE</li>
     * </ul>
     *
     * @param entityClass 实体类，用于获取表结构信息和排序白名单
     * @return 配置好的 Page 对象
     */
    public <T> Page<T> build(Class<T> entityClass) {
        // 处理页码参数：空值或负数使用默认值
        int pageNum = ObjectUtil.defaultIfNull(getPageNum(), DEFAULT_PAGE_NUM);
        if (pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }

        // 处理每页记录数参数：空值或负数使用默认值，超过最大限制时使用最大限制
        int pageSize = ObjectUtil.defaultIfNull(getPageSize(), DEFAULT_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        // 创建分页对象
        Page<T> page = new Page<>(pageNum, pageSize);

        // 构建排序项并添加到分页对象
        List<OrderItem> orderItems = buildOrderItem(entityClass);
        if (CollUtil.isNotEmpty(orderItems)) {
            page.addOrder(orderItems);
        }

        return page;
    }

    /**
     * 构建排序项列表
     * <p>
     * 支持多种排序用法：
     * <ul>
     *     <li>{isAsc:"asc",orderByColumn:"id"} → order by id asc</li>
     *     <li>{isAsc:"asc",orderByColumn:"createTime"} → order by create_time asc</li>
     *     <li>{isAsc:"desc",orderByColumn:"id,name"} → order by id desc, name desc</li>
     *     <li>{isAsc:"asc,desc",orderByColumn:"id,name"} → order by id asc, name desc</li>
     * </ul>
     *
     * <p><b>安全校验：</b>
     * <ul>
     *     <li>参数格式校验：只允许字母、数字、下划线</li>
     *     <li>白名单校验：列名必须在实体类中存在</li>
     *<li>方向校验：只允许 asc 或 desc</li>
     * </ul>
     *
     * @param entityClass 实体类，用于获取表结构信息
     * @return 排序项列表，如果参数无效或列名不在白名单中返回 null
     */
    private List<OrderItem> buildOrderItem(Class<?> entityClass) {
        // 参数空校验：排序字段和方向都不能为空
        if (StrUtil.isBlank(orderByColumn) || StrUtil.isBlank(isAsc)) {
            return null;
        }

        // 格式校验：只允许字母、数字、下划线（防止 SQL 注入）
        if (!COLUMN_PATTERN.matcher(orderByColumn).matches()) {
            return null;
        }
        // 方向格式校验：只允许 asc 或 desc
        if (!DIRECTION_PATTERN.matcher(isAsc.toLowerCase()).matches()) {
            return null;
        }

        // 获取实体类列名映射（白名单）
        Map<String, String> columnMap = getColumnMap(entityClass);
        if (columnMap.isEmpty()) {
            return null;
        }

        // 分割列名和排序方向
        String[] orderByArr = orderByColumn.split(SEPARATOR);
        String[] isAscArr = isAsc.toLowerCase().split(SEPARATOR);

        // 校验方向数组长度：必须为1（统一方向）或与列数一致（每列独立方向）
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            return null;
        }

        // 遍历每个排序列，构建排序项
        List<OrderItem> list = new ArrayList<>();
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByStr = orderByArr[i].trim();

            // 白名单校验：列名必须在实体类中存在
            if (!columnMap.containsKey(orderByStr)) {
                return null;
            }

            // 转换为数据库列名
            orderByStr = columnMap.get(orderByStr);

            // 确定当前列的排序方向
            String isAscStr = isAscArr.length == 1 ? isAscArr[0] : isAscArr[i];
            if (ASC.equalsIgnoreCase(isAscStr)) {
                list.add(OrderItem.asc(orderByStr));
            } else if (DESC.equalsIgnoreCase(isAscStr)) {
                list.add(OrderItem.desc(orderByStr));
            } else {
                return null;
            }
        }

        return list;
    }

    /**
     * 获取实体类列名映射（驼峰属性名 → 数据库列名）
     * <p>
     * 从实体类中提取所有字段的映射关系，用于将用户输入的驼峰格式属性名
     * 转换为数据库列名，实现白名单校验。
     *
     * <p><b>映射示例：</b>
     * <ul>
     *     <li>createTime → create_time</li>
     *     <li>userName → user_name</li>
     *     <li>id → id</li>
     * </ul>
     *
     * @param entityClass 实体类
     * @return 映射 Map：key 为驼峰格式属性名，value 为数据库列名
     */
    private Map<String, String> getColumnMap(Class<?> entityClass) {
        // 获取实体类的表信息（包含所有字段的元数据）
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (ObjectUtil.isNull(tableInfo)) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new HashMap<>();

        // 映射主键字段：property → column
        if (tableInfo.getKeyProperty() != null && tableInfo.getKeyColumn() != null) {
            map.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        }

        // 映射普通字段：遍历所有非主键字段，建立 property → column 映射
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            map.put(fieldInfo.getProperty(), fieldInfo.getColumn());
        }

        return map;
    }

}