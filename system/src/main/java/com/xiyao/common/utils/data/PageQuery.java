package com.xiyao.common.utils.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 分页查询实体类
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
     * 排序列
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc;

    /**
     * 升序
     */
    public static final String ASC = "asc";

    /**
     * 降序
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
     * 分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 合法的列名正则：字母、数字、下划线，多个列名用逗号分隔（不允许空格、点号等）
     */
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*$");

    /**
     * 合法的排序方向正则：asc 或 desc，多个用逗号分隔
     */
    private static final Pattern DIRECTION_PATTERN = Pattern.compile("^(asc|desc)(,(asc|desc))*$", Pattern.CASE_INSENSITIVE);

    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    /**
     * 构建分页对象
     */
    public <T> Page<T> build(Class<T> entityClass) {
        // 获取分页参数
        int pageNum = ObjectUtil.defaultIfNull(getPageNum(), DEFAULT_PAGE_NUM);
        if (pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        int pageSize = ObjectUtil.defaultIfNull(getPageSize(), DEFAULT_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        // 构建分页对象
        Page<T> page = new Page<>(pageNum, pageSize);
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
     * <li>{isAsc:"asc",orderByColumn:"id"} → order by id asc</li>
     * <li>{isAsc:"asc",orderByColumn:"id,createTime"} → order by id asc, create_time asc</li>
     * <li>{isAsc:"desc",orderByColumn:"id,createTime"} → order by id desc, create_time desc</li>
     * <li>{isAsc:"asc,desc",orderByColumn:"id,createTime"} → order by id asc, create_time desc</li>
     * </ul>
     *
     * <p><b>字段格式说明：</b>
     * - 输入支持驼峰格式（createTime）或数据库列名格式（create_time）
     * - 内部会自动转换为数据库列名进行校验和排序
     *
     * @param entityClass 实体类，用于获取表结构信息
     * @return 排序项列表，如果参数无效或列名不在白名单中返回 null
     */
    private List<OrderItem> buildOrderItem(Class<?> entityClass) {
        // 参数空校验
        if (StringUtils.isBlank(orderByColumn) || StringUtils.isBlank(isAsc)) {
            return null;
        }
        // 格式校验：只允许字母、数字、下划线（多列用逗号分隔）
        if (!COLUMN_PATTERN.matcher(orderByColumn).matches()) {
            return null;
        }
        if (!DIRECTION_PATTERN.matcher(isAsc.toLowerCase()).matches()) {
            return null;
        }

        // 获取实体类所有属性和字段名映射（白名单）
        Map<String, String> columnMap = getColumnMap(entityClass);
        if (columnMap.isEmpty()) {
            return null;
        }

        // 分割列名和排序方向
        String[] orderByArr = orderByColumn.split(SEPARATOR);
        String[] isAscArr = isAsc.toLowerCase().split(SEPARATOR);

        // 校验：方向数组长度必须为1（统一方向）或与列数一致（每列独立方向）
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            return null;
        }
        List<OrderItem> list = new ArrayList<>();
        //遍历每个排序列，构建排序项
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByStr = orderByArr[i].trim();
            // 列名映射
            if (!columnMap.containsKey(orderByStr)) {
                return null;
            }
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
     * 获取实体类列名映射（属性名 → 字段名）
     *
     * @param entityClass 实体类
     * @return 映射 Map：key 属性名，value 字段名
     */
    private Map<String, String> getColumnMap(Class<?> entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (ObjectUtil.isNull(tableInfo)) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new HashMap<>();
        // 主键
        if (tableInfo.getKeyProperty() != null && tableInfo.getKeyColumn() != null) {
            map.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        }
        // 普通字段（非主键）
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            map.put(fieldInfo.getProperty(), fieldInfo.getColumn());
        }
        return map;
    }

}
