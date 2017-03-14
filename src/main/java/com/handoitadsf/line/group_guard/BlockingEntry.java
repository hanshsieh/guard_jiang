package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Created by someone on 1/31/2017.
 */
public class BlockingEntry {
    private final String accountId;
    private final Instant createTime;
    public BlockingEntry(@Nonnull String accountId) {
        this.accountId = accountId;
        this.createTime = Instant.now();
    }

    public String getAccountId() {
        return accountId;
    }

    public Instant getCreateTime() {
        return createTime;
    }
}