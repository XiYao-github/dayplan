package com.xiyao.dict.handler;

import cn.hutool.core.util.ObjectUtil;
import com.xiyao.dict.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis 枚举类型转换器
 * <p>
 * 负责 MyBatis 与数据库之间的枚举类型与 JDBC 类型的双向转换。
 * 自动处理所有实现 {@link BaseEnum} 接口的枚举类型。
 *
 * <p>
 * <b>写入数据库（PreparedStatement）：</b>
 * <ul>
 *     <li>将枚举的 code 值写入数据库</li>
 *     <li>支持 Integer、String 等常见类型</li>
 * </ul>
 *
 * <p>
 * <b>从数据库读取（ResultSet）：</b>
 * <ul>
 *     <li>根据数据库中的值查找对应的枚举实例</li>
 *     <li>使用 BaseEnum.fromValue() 进行转换</li>
 * </ul>
 *
 * <p>
 * <b>配置方式（MyBatis 注解）：</b>
 * <pre>{@code
 * public class User {
 *     @TableName
 *     private String tableName = "sys_user";
 *
 *     // 指定 TypeHandler 处理枚举转换
 *     @TableField(typeHandler = EnumTypeHandler.class)
 *     private Status status;
 * }
 * }</pre>
 *
 * @param <E> 枚举类型，必须实现 BaseEnum 接口
 * @author xiyao
 * @see BaseEnum
 */
@NoArgsConstructor
@AllArgsConstructor
public class EnumTypeHandler<E extends BaseEnum<?>> extends BaseTypeHandler<E> {

    /**
     * 目标枚举类的 Class 对象
     */
    private Class<E> type;

    /**
     * 构造函数
     *
     * @param type 枚举类型 Class
     * @throws IllegalArgumentException type 为 null 时抛出
     */
    // public EnumTypeHandler(Class<E> type) {
    //     if (type == null) {
    //         throw new IllegalArgumentException("Type argument cannot be null");
    //     }
    //     this.type = type;
    // }

    /**
     * 设置非 null 参数到 PreparedStatement
     * <p>
     * 将枚举实例的 code 值写入数据库。根据 code 类型调用对应的 PreparedStatement 方法。
     *
     * @param ps        PreparedStatement 对象
     * @param i         参数位置索引
     * @param parameter 枚举参数值
     * @param jdbcType  JDBC 类型（当前未使用）
     * @throws SQLException 数据库操作异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // 获取枚举的存储值（code）
        Object code = parameter.getCode();

        // 根据 code 类型选择合适的 PreparedStatement 方法
        if (code instanceof Integer) {
            // Integer 类型使用 setInt
            ps.setInt(i, (Integer) code);
        } else if (code instanceof String) {
            // String 类型使用 setString
            ps.setString(i, (String) code);
        } else {
            // 其他类型使用 setObject（兜底）
            ps.setObject(i, code);
        }
    }

    /**
     * 从 ResultSet 获取枚举值（通过列名）
     *
     * @param rs         ResultSet 对象
     * @param columnName 列名
     * @return 对应的枚举实例，未找到返回 null
     * @throws SQLException 数据库操作异常
     */
    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从 ResultSet 获取列值
        Object value = rs.getObject(columnName);
        return convert(value);
    }

    /**
     * 从 ResultSet 获取枚举值（通过列索引）
     *
     * @param rs         ResultSet 对象
     * @param columnIndex 列索引
     * @return 对应的枚举实例，未找到返回 null
     * @throws SQLException 数据库操作异常
     */
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从 ResultSet 获取列值
        Object value = rs.getObject(columnIndex);
        return convert(value);
    }

    /**
     * 从 CallableStatement 获取枚举值
     *
     * @param cs         CallableStatement 对象
     * @param columnIndex 列索引
     * @return 对应的枚举实例，未找到返回 null
     * @throws SQLException 数据库操作异常
     */
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从 CallableStatement 获取列值
        Object value = cs.getObject(columnIndex);
        return convert(value);
    }

    /**
     * 将数据库值转换为枚举实例
     * <p>
     * 使用 BaseEnum.fromValue() 进行转换，支持 code、desc、name 三种匹配方式。
     *
     * @param value 数据库中的值
     * @return 对应的枚举实例，value 为 null 或未找到返回 null
     */
    private E convert(Object value) {
        // null 值直接返回 null
        if (ObjectUtil.isNull(value)) {
            return null;
        }
        // 委托给 BaseEnum.fromValue 进行转换
        return BaseEnum.fromValue(type, value);
    }
}