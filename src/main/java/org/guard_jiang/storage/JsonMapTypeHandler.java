package org.guard_jiang.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by someone on 4/3/2017.
 */
public class JsonMapTypeHandler extends BaseTypeHandler<Map<String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapTypeHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TypeReference<HashMap<String,Object>> MAP_TYPE
            = new TypeReference<HashMap<String,Object>>() {};

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int paramIdx,
            Map<String, String> map,
            JdbcType jdbcType) throws SQLException {
        try {
            String jsonStr = objectMapper.writeValueAsString(map);
            ps.setString(paramIdx, jsonStr);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Fail to convert map string json", ex);
        }
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        if (rs.wasNull()) {
            return Collections.emptyMap();
        }
        return convertJsonMap(str);
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        if (rs.wasNull()) {
            return Collections.emptyMap();
        }
        return convertJsonMap(str);
    }

    private Map<String, String> convertJsonMap(@Nonnull String str) {
        try {
            return objectMapper.readValue(str, MAP_TYPE);
        } catch (Exception ex) {
            LOGGER.error("Fail to parse as map. str: {}", str);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        if (cs.wasNull()) {
            return Collections.emptyMap();
        }
        return convertJsonMap(str);
    }
}
