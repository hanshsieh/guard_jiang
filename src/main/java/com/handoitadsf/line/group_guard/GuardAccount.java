package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by someone on 3/22/2017.
 */
public class GuardAccount {
    private final Storage storage;
    private final String mid;
    public GuardAccount(@Nonnull Storage storage, @Nonnull String mid) {
        this.storage = storage;
        this.mid = mid;
    }
    public String getMid() {
        return mid;
    }

    @Nonnull
    public AccountCredential getCredential() throws IOException {
        return storage.getAccountCredential(mid);
    }

    public void setCredential(AccountCredential credential) throws IOException {
        storage.setAccountCredential(mid, credential);
    }
}
