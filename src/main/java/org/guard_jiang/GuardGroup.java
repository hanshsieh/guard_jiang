package org.guard_jiang;

import org.guard_jiang.storage.GroupMetadata;
import org.guard_jiang.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
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

    public void addAdmin(@Nonnull String admin) throws IOException {
        LOGGER.debug("Adding {} as admin of group {}", admin, id);
        storage.setGroupRole(id, admin, Role.ADMIN);
    }

    @Nonnull
    public Collection<BlockingRecord> getBlockingRecords() throws IOException {
        return storage.getGroupBlockingRecords(id);
    }

    public void putBlockingRecord(@Nonnull BlockingRecord blockingRecord) throws IOException {
        LOGGER.debug("Adding user {} to black list of group {}", blockingRecord.getUserId(), id);
        storage.setGroupBlockingRecord(id, blockingRecord);
    }

    @Nonnull
    public Set<String> getMembersBackup() throws IOException {
        return storage.getGroupMembersBackup(id);
    }

    public void setMembersBackup(@Nonnull Set<String> members) throws IOException {
        LOGGER.debug("Backing up members of group {}: {}", id, members);
        storage.setGroupMembersBackup(id, members);
    }

    public GroupMetadata getMetadata() throws IOException {
        GroupMetadata metadata = storage.getGroupMetadata(id);
        if (metadata == null) {
            return new GroupMetadata();
        }
        return metadata;
    }

    public void setMetadata(@Nonnull GroupMetadata metadata) throws IOException {
        LOGGER.debug("Setting metadata group {}", id);
        storage.setGroupMetadata(id, metadata);
    }
}
