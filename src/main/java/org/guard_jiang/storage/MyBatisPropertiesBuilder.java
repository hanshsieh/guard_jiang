package org.guard_jiang.storage;

import com.typesafe.config.Config;
import org.apache.http.client.utils.URIBuilder;
import org.guard_jiang.KeyService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * Properties for MyBatis.
 */
public class MyBatisPropertiesBuilder {

    private final KeyService keyService;

    public MyBatisPropertiesBuilder() throws IOException {
        this(new KeyService());
    }

    /**
     * Constructor a new instance.
     *
     * @param keyService Key service instance
     */
    public MyBatisPropertiesBuilder(@Nonnull KeyService keyService) {
        this.keyService = keyService;
    }

    /**
     * Build the properties from the given config.
     *
     * @param config Config.
     *
     * @return Properties.
     */
    @Nonnull
    public Properties build(@Nonnull Config config) throws IOException {
        Properties properties = new Properties();
        try {
            URI url = new URIBuilder()
                    .setScheme(config.getString("scheme"))
                    .setHost(config.getString("host"))
                    .setPort(config.getInt("port"))
                    .setPath("/" + config.getString("database"))
                    .build();

            String password = "";
            if (config.hasPath("passwordKeyName")) {
                String passwordKey = config.getString("passwordKeyName");
                password = keyService.getSecret(passwordKey);
            }

            properties.put("url", url.toASCIIString());
            properties.put("username", config.getString("userName"));
            properties.put("password", password);
            properties.put("maxLifetime", config.getString("maxLifetime"));
            properties.put("idleTimeout", config.getString("idleTimeout"));
            properties.put("minimumIdle", config.getString("minimumIdle"));
            properties.put("maximumPoolSize", config.getString("maximumPoolSize"));
            properties.put("maximumPoolSize", config.getString("maximumPoolSize"));
            properties.put("readOnly", config.getString("readOnly"));
            properties.put("connectionTimeout", config.getString("connectionTimeout"));
        } catch (Exception ex) {
            throw new IOException("Fail to load from the config", ex);
        }
        return properties;
    }
}

