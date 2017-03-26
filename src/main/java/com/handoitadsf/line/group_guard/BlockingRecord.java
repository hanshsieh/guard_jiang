package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Created by someone on 1/31/2017.
 */
public class BlockingRecord {
    private static final long DEFAULT_BLOCKING_MS = 1000 * 60 * 60;
    private final String accountId;
    private final Instant blockUntilTime;
    public BlockingRecord(@Nonnull String accountId) {
        this.accountId = accountId;
        this.blockUntilTime = Instant.ofEpochMilli(System.currentTimeMillis() + DEFAULT_BLOCKING_MS);
    }

    public String getAccountId() {
        return accountId;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > blockUntilTime.toEpochMilli();
    }
}
