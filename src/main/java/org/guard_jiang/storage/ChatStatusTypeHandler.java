package org.guard_jiang.storage;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by someone on 4/3/2017.
 */
public class ChatStatusTypeHandler extends BaseTypeHandler<ChatStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIdx, ChatStatus status, JdbcType jdbcType) throws SQLException {
        ps.setInt(paramIdx, status.getId());
    }

    @Override
    public ChatStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int statusId = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return convertChatStatus(statusId);
    }

    @Override
    public ChatStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int statusId = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return convertChatStatus(statusId);
    }

    @Nonnull
    private ChatStatus convertChatStatus(int statusId) {
        return ChatStatus.fromId(statusId);
    }

    @Override
    public ChatStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int statusId = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return convertChatStatus(statusId);
    }
}
