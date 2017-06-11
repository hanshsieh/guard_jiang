package org.guard_jiang.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Data source factory for Hikari connection pool.
 */
public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
    public HikariDataSourceFactory() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/mysql.properties")){
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Fail to load MySQL properties", ex);
        }
        hikariDataSource.setDataSourceProperties(properties);
        dataSource = hikariDataSource;
    }
}
