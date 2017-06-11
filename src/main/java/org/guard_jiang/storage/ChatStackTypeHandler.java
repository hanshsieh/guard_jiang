package org.guard_jiang.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.guard_jiang.chat.ChatFrame;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by someone on 4/3/2017.
 */
public class ChatStackTypeHandler extends BaseTypeHandler<Deque<ChatFrame>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String KEY_FRAME_STATUS= "status";
    private static final String KEY_FRAME_DATA = "data";

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int paramIdx,
            Deque<ChatFrame> stack,
            JdbcType jdbcType) throws SQLException {
        try {
            ArrayNode stackJson = objectMapper.createArrayNode();
            for (ChatFrame chatFrame : stack) {
                ChatStatus chatStatus = chatFrame.getChatStatus();
                ObjectNode data = chatFrame.getData();

                ObjectNode frameJson = objectMapper.createObjectNode();
                frameJson.put(KEY_FRAME_STATUS, chatStatus.getId());
                frameJson.set(KEY_FRAME_DATA, data);

                stackJson.add(frameJson);
            }
            String jsonStr = objectMapper.writeValueAsString(stackJson);
            ps.setString(paramIdx, jsonStr);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Fail to convert map string json", ex);
        }
    }

    @Override @Nullable
    public Deque<ChatFrame> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return convertJsonStack(str);
    }

    @Override @Nullable
    public Deque<ChatFrame> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return convertJsonStack(str);
    }

    private Deque<ChatFrame> convertJsonStack(@Nonnull String str) throws SQLException {
        JsonNode stackJson;
        try {
            stackJson = objectMapper.readTree(str);
        } catch (IOException ex) {
            throw new SQLException("Fail to parse the value as JSON string", ex);
        }
        Deque<ChatFrame> stack = new ArrayDeque<>();
        for (JsonNode frameJson : stackJson) {
            int statusId = frameJson.get(KEY_FRAME_STATUS).asInt();
            ObjectNode data = (ObjectNode) frameJson.get(KEY_FRAME_DATA);
            stack.addLast(new ChatFrame(ChatStatus.fromId(statusId), data));
        }
        return stack;
    }

    @Override @Nullable
    public Deque<ChatFrame> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return convertJsonStack(str);
    }
}
