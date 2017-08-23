package org.guard_jiang.services.storage.sql.records;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Group blocking record.
 */
public class BlockingRecord {
    private long id;
    private final String groupId;
    private final String userId;
    private Instant expiryTime;

    public BlockingRecord(
            long id,
            @Nonnull String groupId,
            @Nonnull String userId,
            @Nullable Instant expiryTime) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.expiryTime = expiryTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setExpiryTime(@Nullable Instant expiryTime) {
        this.expiryTime = expiryTime;
    }
}
