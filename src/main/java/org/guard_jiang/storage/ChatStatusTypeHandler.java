package org.guard_jiang.storage;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.guard_jiang.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Created by someone on 4/3/2017.
 */
public class ChatStatusTypeHandler extends BaseTypeHandler<ChatStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatStatusTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIdx, ChatStatus status, JdbcType jdbcType) throws SQLException {
        ps.setInt(paramIdx, status.getId());
    }

    @Override
    public ChatStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int statusId = rs.getInt(columnName);
        if (rs.wasNull()) {
            return ChatStatus.NONE;
        }
        return convertChatStatus(statusId);
    }

    @Override
    public ChatStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int statusId = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return ChatStatus.NONE;
        }
        return convertChatStatus(statusId);
    }

    private ChatStatus convertChatStatus(int statusId) {
        try {
            return ChatStatus.fromId(statusId);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Unexpected value for chat status, {}. Default to NONE.", statusId);
            return ChatStatus.NONE;
        }
    }

    @Override
    public ChatStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int statusId = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return ChatStatus.NONE;
        }
        return convertChatStatus(statusId);
    }
}
