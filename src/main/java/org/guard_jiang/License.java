package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Created by someone on 4/15/2017.
 */
public class License {
    private long id;
    private final String key;
    private final String userId;
    private final Instant createTime;
    private Instant expiryTime;
    private int maxDefenders;
    private int maxSupporters;
    private int numDefenders;
    private int numSupporters;

    public License(
            long id,
            @Nonnull String key,
            @Nonnull String userId,
            @Nonnull Instant createTime) {
        this.id = id;
        this.key = key;
        this.userId = userId;
        this.createTime = createTime;
    }

    public License(
            @Nonnull String key,
            @Nonnull String userId,
            @Nonnull Instant createTime) {
        this(-1L, key, userId, createTime);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
