package org.guard_jiang;

import javax.annotation.Nonnull;
import java.security.SecureRandom;

/**
 * Created by someone on 4/15/2017.
 */
public class LicenseKeyProvider {
    private static final int KEY_LEN = 40;
    private static final int ALPHABET_SIZE = 26;
    private static final int CHUNK_SIZE = 5;
    private static final char DELIMITER = '-';

    private final SecureRandom random;
    public LicenseKeyProvider(@Nonnull SecureRandom random) {
        this.random = random;
    }
    public LicenseKeyProvider() {
        this(new SecureRandom());
    }
    public String buildLicenseKey() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < KEY_LEN; ++i) {
            char ch = (char)('A' + random.nextInt(ALPHABET_SIZE));
            builder.append(ch);
        }
        return builder.toString();
    }

    @Nonnull
    public String normalize(@Nonnull String key) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            if (ch == DELIMITER) {
                continue;
            }
            builder.append(Character.toUpperCase(ch));
        }
        return builder.toString();
    }

    @Nonnull
    public String toReadableForm(@Nonnull String key) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < key.length(); ++i) {
            if (i > 0 && (i % CHUNK_SIZE) == 0) {
                builder.append(DELIMITER);
            }
            builder.append(key.charAt(i));
        }
        return builder.toString();
    }
}
