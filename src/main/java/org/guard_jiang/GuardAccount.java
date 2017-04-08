package org.guard_jiang;

import org.guard_jiang.storage.Storage;

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
    public Credential getCredential() throws IOException {
        return storage.getCredential(mid);
    }

    public void setCredential(Credential credential) throws IOException {
        storage.setCredential(mid, credential);
    }
}
