package org.guard_jiang.services.license;

import javax.annotation.Nonnull;
import java.security.SecureRandom;

/**
 * This class is used for creating license key.
 */
public class LicenseKeyService {
    private static final int KEY_LEN = 40;
    private static final int ALPHABET_SIZE = 26;

    /**
     * TODO {@link SecureRandom} is thread-safe, but sharing it with multi-thread may hurt performance.
     * https://stackoverflow.com/questions/1461568/is-securerandom-thread-safe
     */
    private final SecureRandom random;
    public LicenseKeyService(@Nonnull SecureRandom random) {
        this.random = random;
    }
    public LicenseKeyService() {
        this(new SecureRandom());
    }

    @Nonnull
    public String genLicenseKey() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < KEY_LEN; ++i) {
            char ch = (char)('A' + random.nextInt(ALPHABET_SIZE));
            builder.append(ch);
        }
        return builder.toString();
    }
}
