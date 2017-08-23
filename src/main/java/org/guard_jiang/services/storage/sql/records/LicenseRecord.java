package org.guard_jiang.services.storage.sql.records;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * A license gives a user the right to assign some guard accounts as defenders or
 * supporters.
 */
public class LicenseRecord {

    private String id;
    private String key;
    private String userId;
    private Instant createdTime;
    private Instant expiredTime;
    private int maxDefenders = 0;
    private int maxSupporters = 0;
    private int numDefenders = 0;
    private int numSupporters = 0;
    private int maxAdmins = 0;
    private int numAdmins = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Instant expiredTime) {
        this.expiredTime = expiredTime;
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

    public int getMaxAdmins() {
        return maxAdmins;
    }

    public void setMaxAdmins(int maxAdmins) {
        this.maxAdmins = maxAdmins;
    }

    public int getNumAdmins() {
        return numAdmins;
    }

    public void setNumAdmins(int numAdmins) {
        this.numAdmins = numAdmins;
    }
}
