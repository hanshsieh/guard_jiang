package org.guard_jiang.storage;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by someone on 4/3/2017.
 */
public class StorageEnvTypeHandler extends BaseTypeHandler<StorageEnv> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageEnvTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIdx, StorageEnv env, JdbcType jdbcType) throws SQLException {
        ps.setInt(paramIdx, env.getId());
    }

    @Override
    public StorageEnv getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int envId = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return convertEnv(envId);
    }

    @Override
    public StorageEnv getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int envId = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return convertEnv(envId);
    }

    private StorageEnv convertEnv(int envId) {
        try {
            return StorageEnv.fromId(envId);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Unexpected value for chat status, {}. Default to NONE.", envId);
            return null;
        }
    }

    @Override
    public StorageEnv getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int envId = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return convertEnv(envId);
    }
}
