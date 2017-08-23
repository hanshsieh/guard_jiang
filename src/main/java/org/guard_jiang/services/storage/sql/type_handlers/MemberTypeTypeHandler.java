package org.guard_jiang.services.storage.sql.type_handlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.guard_jiang.chat.ChatEnvType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by someone on 4/3/2017.
 */
public class MemberTypeTypeHandler extends BaseTypeHandler<ChatEnvType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIdx, ChatEnvType chatEnvType, JdbcType jdbcType) throws SQLException {
        ps.setInt(paramIdx, chatEnvType.getId());
    }

    @Override
    public ChatEnvType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int typeId = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return ChatEnvType.fromId(typeId);
    }

    @Override
    public ChatEnvType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int typeId = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return ChatEnvType.fromId(typeId);
    }

    @Override
    public ChatEnvType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int typeId = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return ChatEnvType.fromId(typeId);
    }
}
