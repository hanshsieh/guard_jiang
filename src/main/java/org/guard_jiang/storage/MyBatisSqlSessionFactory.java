package org.guard_jiang.storage;

import com.typesafe.config.Config;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Created by icand on 2017/6/4.
 */
public class MyBatisSqlSessionFactory {

    private static final String CONFIG_FILE = "mybatis.xml";

    private SqlSessionFactory readSessionFactory;
    private SqlSessionFactory writeSessionFactory;

    public MyBatisSqlSessionFactory(
            @Nonnull Config readConfig,
            @Nonnull Config writeConfig) throws IOException {
        this(readConfig, writeConfig, new MyBatisPropertiesBuilder());
    }

    public MyBatisSqlSessionFactory(
            @Nonnull Config readConfig,
            @Nonnull Config writeConfig,
            @Nonnull MyBatisPropertiesBuilder propertiesBuilder) throws IOException {
        this(createSessionFactory(readConfig, propertiesBuilder),
            createSessionFactory(writeConfig, propertiesBuilder));
    }

    public MyBatisSqlSessionFactory(
            @Nonnull SqlSessionFactory readSessionFactory,
            @Nonnull SqlSessionFactory writeSessionFactory) throws IOException {
        this.readSessionFactory = readSessionFactory;
        this.writeSessionFactory = writeSessionFactory;
    }

    private static SqlSessionFactory createSessionFactory(
            @Nonnull Config config,
            @Nonnull MyBatisPropertiesBuilder propertiesBuilder) throws IOException {
        try (Reader configReader = Resources.getResourceAsReader(CONFIG_FILE)) {
            Properties properties = propertiesBuilder.build(config);
            return new SqlSessionFactoryBuilder().build(configReader, properties);
        }
    }

    @Nonnull
    public SqlSession openReadSession() {
        return readSessionFactory.openSession();
    }

    @Nonnull
    public SqlSession openWriteSession() {
        return writeSessionFactory.openSession();
    }
}
