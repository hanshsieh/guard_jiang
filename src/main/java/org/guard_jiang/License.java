package org.guard_jiang;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Created by someone on 4/15/2017.
 */
public class License {
    private final String key;
    private String userId;
    private Instant createTime;
    private Instant expiryTime;
    private int maxDefenders;
    private int maxSupporters;
    private int numDefenders;
    private int numSupporters;

    public License(@Nonnull String key) {
        this.key = key;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getKey() {
        return key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
