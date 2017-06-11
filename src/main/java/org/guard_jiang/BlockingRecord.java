package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Created by someone on 1/31/2017.
 */
public class BlockingRecord {
    private final String groupId;
    private final String userId;
    private final Instant expiryTime;
    public BlockingRecord(@Nonnull String groupId, @Nonnull String userId, @Nullable Instant expiryTime) {
        this.groupId = groupId;
        this.userId = userId;
        this.expiryTime = expiryTime;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Nonnull
    public String getUserId() {
        return userId;
    }

    @Nullable
    public Instant getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        if (expiryTime != null) {
            return System.currentTimeMillis() > expiryTime.toEpochMilli();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "userId: " + userId + ", expiryTime: " + expiryTime;
    }
}
