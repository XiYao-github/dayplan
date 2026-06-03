package com.xiyao.dict.handler;

import com.xiyao.dict.enums.BaseEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用枚举 TypeHandler
 * <p>
 * 自动处理所有实现 BaseEnum 接口的枚举类型与数据库 int 类型的转换
 *
 * @param <E> 枚举类型，必须实现 BaseEnum 接口
 */
public class EnumTypeHandler<E extends BaseEnum<?>> extends BaseTypeHandler<E> {

    private final Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举转换为 code 存入数据库
        Object code = parameter.getCode();
        if (code instanceof Integer) {
            ps.setInt(i, (Integer) code);
        } else if (code instanceof String) {
            ps.setString(i, (String) code);
        } else {
            ps.setObject(i, code);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return convert(value);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return convert(value);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return convert(value);
    }


    private E convert(Object value) {
        if (value == null) {
            return null;
        }
        // 使用 BaseEnum 的静态方法进行转换
        return BaseEnum.fromValue(type, value);
    }
}