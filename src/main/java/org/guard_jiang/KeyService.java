package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A service used for getting secret key.
 */
public class KeyService {
    private final static String KEY_FILE = "/secret.properties";
    private final Properties properties;

    public KeyService() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(KEY_FILE)) {
            if (input == null) {
                throw new IOException("Fail to open file \"" + KEY_FILE + "\"");
            }
            properties = new Properties();
            properties.load(input);
        }
    }

    public KeyService(@Nonnull Properties properties) {
        this.properties = properties;
    }

    @Nonnull
    public String getSecret(@Nonnull String keyName) throws IOException, IllegalArgumentException {
        String value = properties.getProperty(keyName);
        if (value == null) {
            throw new IllegalArgumentException("Secret with name \"" + keyName + "\" isn't found");
        }
        return value;
    }
}
