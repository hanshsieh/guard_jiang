package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * A license gives a user the right to assign some guard accounts as defenders or
 * supporters.
 */
public class License {

    private static final int CHUNK_SIZE = 5;
    private static final char DELIMITER = '-';

    private String id;
    private final String key;
    private final String userId;
    private final Instant createTime;
    private Instant expiryTime = null;
    private int maxDefenders = 0;
    private int maxSupporters = 0;
    private int numDefenders = 0;
    private int numSupporters = 0;

    public License(
            @Nullable String id,
            @Nonnull String key,
            @Nonnull String userId,
            @Nonnull Instant createTime) {
        this.id = id;
        this.key = key;
        this.userId = userId;
        this.createTime = createTime;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nonnull
    public Instant getCreateTime() {
        return createTime;
    }

    @Nullable
    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(@Nullable Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public String getUserId() {
        return userId;
    }

    public int getMaxDefenders() {
        return maxDefenders;
    }

    public void setMaxDefenders(int maxDefenders) {
        this.maxDefenders = maxDefenders;
    }

    public int getMaxSupporters() {
        return maxSupporters;
    }

    public void setMaxSupporters(int maxSupporters) {
        this.maxSupporters = maxSupporters;
    }

    public int getNumDefenders() {
        return numDefenders;
    }

    public void setNumDefenders(int numDefenders) {
        this.numDefenders = numDefenders;
    }

    public int getNumSupporters() {
        return numSupporters;
    }

    public void setNumSupporters(int numSupporters) {
        this.numSupporters = numSupporters;
    }

    @Nonnull
    public String getReadableKey() {
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
