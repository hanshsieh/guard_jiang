package org.guard_jiang.services.storage.sql.type_handlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Created by someone on 4/3/2017.
 */
public class InstantTypeHandler extends BaseTypeHandler<Instant> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIdx, Instant instant, JdbcType jdbcType) throws SQLException {
        ps.setLong(paramIdx, instant.toEpochMilli());
    }

    @Override
    public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long epochMillis = rs.getLong(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    @Override
    public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long epochMillis = rs.getLong(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    @Override
    public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long epochMillis = cs.getLong(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis);
    }
}
