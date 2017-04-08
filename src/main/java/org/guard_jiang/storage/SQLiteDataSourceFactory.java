package org.guard_jiang.storage;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.sqlite.SQLiteConfig;

import java.util.Properties;

/**
 * Created by someone on 4/2/2017.
 */
public class SQLiteDataSourceFactory extends PooledDataSourceFactory {
    public SQLiteDataSourceFactory() {
        PooledDataSource pooledDataSource = new PooledDataSource();
        Properties properties = new Properties();
        properties.setProperty(SQLiteConfig.Pragma.FOREIGN_KEYS.pragmaName, "true");
        pooledDataSource.setDriverProperties(properties);
        dataSource = pooledDataSource;
    }
}
