package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 3/23/2017.
 */
public class GuardGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardGroup.class);
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
        LOGGER.debug("Setting admins of group {} to {}", id, adminIds);
        storage.setGroupAdmins(id, adminIds);
    }

    @Nonnull
    public Collection<BlockingRecord> getBlockingRecords() throws IOException {
        return storage.getGroupBlockingRecords(id);
    }

    public void putBlockingRecord(@Nonnull BlockingRecord blockingRecord) throws IOException {
        LOGGER.debug("Adding user {} to black list of group {}", blockingRecord.getAccountId(), id);
        storage.putGroupBlockingRecord(id, blockingRecord);
    }

    @Nonnull
    public MembersBackup getMembersBackup() throws IOException {
        return storage.getGroupMembersBackup(id);
    }

    public void setMembersBackup(@Nonnull MembersBackup backup) throws IOException {
        LOGGER.debug("Backing up members of group {}: {}", id, backup.getMembers());
        storage.setGroupMembersBackup(id, backup);
    }

    @Nullable
    public Instant getRecoveryExpiryTime() throws IOException {
        return storage.getGroupRecoveryExpiryTime(id);
    }

    public void setRecoveryExpiryTime(@Nonnull Instant expiryTime) throws IOException {
        LOGGER.debug("Setting recovery expiry time for group {} to {}", id, expiryTime);
        storage.setGroupRecoveryExpiryTime(id, expiryTime);
    }
}
