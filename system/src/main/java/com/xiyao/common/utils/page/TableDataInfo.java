package com.xiyao.common.utils.page;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 * <p>
 * 前后端分页数据交互的统一格式。
 * 通常与 Element Plus、Ant Design 等前端 UI 框架的表格组件配合使用。
 *
 * <p>
 * <b>响应格式：</b>
 * <pre>{@code
 * {
 *     "code": 200,
 *     "msg": "查询成功",
 *     "total": 100,
 *     "rows": [...]
 * }
 * }</pre>
 *
 * @author xiyao
 */
@Data
@NoArgsConstructor
public class TableDataInfo<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> rows;

    /**
     * 状态码（HTTP 200）
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 构造函数
     *
     * @param list  数据列表
     * @param total 总记录数
     */
    public TableDataInfo(List<T> list, long total) {
        this.rows = list;
        this.total = total;
        this.code = HttpStatus.HTTP_OK;
        this.msg = "查询成功";
    }

    /**
     * 根据 MyBatis-Plus 分页对象构建
     *
     * @param page 分页对象
     * @param <T>  数据类型
     * @return 分页数据
     */
    public static <T> TableDataInfo<T> build(IPage<T> page) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * 根据数据列表构建（无分页）
     *
     * @param list 数据列表
     * @param <T> 数据类型
     * @return 分页数据
     */
    public static <T> TableDataInfo<T> build(List<T> list) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    /**
     * 构建空的表格数据
     *
     * @param <T> 数据类型
     * @return 空分页数据
     */
    public static <T> TableDataInfo<T> build() {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        return rspData;
    }

    /**
     * 根据数据列表和分页参数构建（用于假分页）
     *
     * @param list 原始数据列表（全部数据）
     * @param page 分页参数对象
     * @param <T>  数据类型
     * @return 分页数据
     */
    public static <T> TableDataInfo<T> build(List<T> list, IPage<T> page) {
        if (CollUtil.isEmpty(list)) {
            return TableDataInfo.build();
        }
        List<T> pageList = CollUtil.page((int) page.getCurrent() - 1, (int) page.getSize(), list);
        return new TableDataInfo<>(pageList, list.size());
    }
}