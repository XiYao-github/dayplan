package com.xiyao.framework.handle;

import com.xiyao.common.utils.Sm4Util;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import java.sql.*;

public class EncryptDataHandler extends BaseTypeHandler<String> {

    /**
     * 写入数据库(insert update)：明文 → 密文
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (StringUtils.hasText(parameter)) {
            String encrypted = Sm4Util.encrypt(parameter);
            ps.setString(i, encrypted);
        } else {
            ps.setString(i, parameter);
        }
    }

    /**
     * 数据库查询(列名)：密文 → 明文
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encrypted = rs.getString(columnName);
        return Sm4Util.decrypt(encrypted);
    }

    /**
     * 数据库查询(列索引)：密文 → 明文
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return Sm4Util.decrypt(encrypted);
    }

    /**
     * 数据库查询(存储过程)：密文 → 明文
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return Sm4Util.decrypt(encrypted);
    }
}