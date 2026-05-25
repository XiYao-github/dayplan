package com.xiyao.common.base.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity 基类
 * <p>
 * 所有业务实体类应继承此类，获取以下基础能力：
 * <ul>
 *     <li>通用审计字段：创建人、创建时间、更新人、更新时间</li>
 *     <li>逻辑删除支持：deleted 字段 + @TableLogic 注解</li>
 *     <li>乐观锁支持：version 字段 + @Version 注解</li>
 *     <li>分页插件需要的扩展字段：searchValue、params</li>
 * </ul>
 *
 * <p>
 * <b>字段说明：</b>
 * <ul>
 *     <li>id：自增主键，用于关联和索引</li>
 *     <li>create_by/create_time：记录数据创建时的操作人/时间，MyBatis-Plus 自动填充</li>
 *     <li>update_by/update_time：记录数据更新时的操作人/时间，MyBatis-Plus 自动填充</li>
 *     <li>delete_time：软删除时间，非空表示已删除</li>
 *     <li>deleted：逻辑删除标志，0=未删除，1=已删除</li>
 *     <li>version：乐观锁版本号，并发更新时防止数据覆盖</li>
 * </ul>
 *
 * @author xiyao
 * @see IdType
 * @see TableLogic
 * @see Version
 */
@Data
@FieldNameConstants
public class MyBaseEntity implements Serializable {

    /** 序列化版本号 */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     * <p>
     * 用于 MyBatis-Plus 分页插件的条件查询，
     * 不参与序列化（JSON 输出时忽略）。
     */
    @JsonIgnore
    @TableField(exist = false)
    private String searchValue;

    /**
     * 请求参数
     * <p>
     * 用于传递分页、排序等扩展参数，
     * 仅在非空时序列化到 JSON。
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

    /**
     * 主键 ID
     * <p>
     * 使用自增策略，数据库表需设置 AUTO_INCREMENT。
     * 实体类中使用 Long 类型避免 int 的并发问题。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 备注
     * <p>
     * 用于记录数据的相关说明或备注信息。
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建者 ID
     * <p>
     * 记录数据创建人的用户 ID，
     * 配合 SecurityUtils 可获取当前登录用户 ID。
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     * <p>
     * 记录数据创建的时间戳，
     * 格式化输出为 "yyyy-MM-dd HH:mm:ss" 时区东八区。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     * <p>
     * 记录数据最近一次更新的用户 ID。
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     * <p>
     * 记录数据最近一次更新的时间戳。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除时间
     * <p>
     * 软删除时记录删除时间，
     * 与 deleted 字段配合实现逻辑删除。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 删除标志
     * <p>
     * 逻辑删除标志：
     * <ul>
     *     <li>0：未删除（正常数据）</li>
     *     <li>1：已删除（逻辑删除）</li>
     * </ul>
     * MyBatis-Plus 查询时会自动拼接 deleted = 0 条件。
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     * <p>
     * 用于解决并发更新时的数据覆盖问题。
     * 更新时会自动将 version + 1，并校验 WHERE version = oldVersion。
     * 若版本不匹配说明有其他线程并发更新，则抛出乐观锁异常。
     */
    @Version
    @TableField("version")
    private Integer version;
}