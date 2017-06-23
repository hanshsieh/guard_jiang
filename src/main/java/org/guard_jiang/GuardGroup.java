package org.guard_jiang;

import org.guard_jiang.storage.GroupMetadata;
import org.guard_jiang.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 3/23/2017.
 */
public class GuardGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardGroup.class);
    private final Storage storage;
    private final String id;
    public GuardGroup(@Nonnull Storage storage, @Nonnull String groupId) {
        this.storage = storage;
        this.id = groupId;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public List<GroupRole> getRoles() throws IOException {
        return getRoles(null);
    }

    @Nonnull
    public List<GroupRole> getRoles(@Nullable Role role) throws IOException {
        return storage.getRolesOfGroup(id, role);
    }

    @Nullable
    public GroupRole getRoleOfUser(@Nonnull String userId) throws IOException {
        return storage.getGroupRoleOfUser(id, userId);
    }

    public void addRole(@Nonnull String userId, @Nonnull Role role, @Nonnull String licenseId)
        throws IOException {
        GroupRole groupRole = new GroupRole(null, id, userId, role, licenseId);
        LOGGER.debug("Adding {} as {} in group {}", groupRole.getUserId(), groupRole.getRole(), id);
        storage.addGroupRole(groupRole);
    }

    @Nonnull
    public Collection<BlockingRecord> getBlockingRecords() throws IOException {
        return storage.getGroupBlockingRecords(id);
    }

    public void blockUser(@Nonnull String userId, @Nullable Instant expiryTs) throws IOException {
        LOGGER.debug("Adding user {} to black list of group {}", userId, id);
        BlockingRecord record = new BlockingRecord(id, userId, expiryTs);
        storage.setGroupBlockingRecord(record);
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
            return new GroupMetadata(id);
        }
        return metadata;
    }

    public void setMetadata(@Nonnull GroupMetadata metadata) throws IOException {
        if (!id.equals(metadata.getGroupId())) {
            throw new IllegalArgumentException("Group ID mismatch");
        }
        LOGGER.debug("Setting metadata group {}", id);
        storage.setGroupMetadata(metadata);
    }
}
