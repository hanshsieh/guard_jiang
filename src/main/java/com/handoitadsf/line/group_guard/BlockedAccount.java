package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Created by someone on 1/31/2017.
 */
public class BlockedAccount {
    private final String accountId;
    private final Instant createTime;
    public BlockedAccount(@Nonnull String accountId) {
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
