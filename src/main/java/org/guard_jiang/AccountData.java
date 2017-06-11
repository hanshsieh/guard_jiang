package org.guard_jiang;

import org.guard_jiang.storage.Storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by someone on 3/22/2017.
 */
public class AccountData {
    private final String mid;
    private final int partition;
    private Credential credential;
    public AccountData(@Nonnull String mid, Integer partition) {
        this.mid = mid;
        this.partition = partition;
    }

    @Nonnull
    public String getMid() {
        return mid;
    }

    @Nullable
    public Credential getCredential() {
        return credential;
    }

    public void setCredential(@Nullable Credential credential) {
        this.credential = credential;
    }

    public int getPartition() {
        return partition;
    }

}
