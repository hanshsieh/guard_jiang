package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 3/23/2017.
 */
public class GuardGroup {
    private final Storage storage;
    private final String id;
    public GuardGroup(@Nonnull Storage storage, @Nonnull String id) {
        this.storage = storage;
        this.id = id;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Map<String, Role> getRoles() throws IOException {
        return storage.getGroupRoles(id);
    }

    @Nonnull
    public Set<String> getAdmins() throws IOException {
        return storage.getGroupAdminIds(id);
    }

    public void setAdmins(@Nonnull Set<String> adminIds) throws IOException {
        storage.setGroupAdmins(id, adminIds);
    }

    @Nonnull
    public Collection<BlockingRecord> getBlockingRecords() throws IOException {
        return storage.getGroupBlockingRecords(id);
    }

    public void putBlockingRecord(@Nonnull BlockingRecord blockingRecord) throws IOException {
        storage.putGroupBlockingRecord(id, blockingRecord);
    }
}
