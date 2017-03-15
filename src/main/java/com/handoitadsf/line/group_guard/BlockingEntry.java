package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Created by someone on 1/31/2017.
 */
public class BlockingEntry {
    private static final long DEFAULT_BLOCKING_MS = 1000 * 60 * 10;
    private final String accountId;
    private final Instant createTime;
    private final long blockingMs;
    public BlockingEntry(@Nonnull String accountId) {
        this.accountId = accountId;
        this.createTime = Instant.now();
        blockingMs = DEFAULT_BLOCKING_MS;
    }

    public String getAccountId() {
        return accountId;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime.toEpochMilli() > blockingMs;
    }
}
