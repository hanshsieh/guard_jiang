package org.guard_jiang.storage;

import org.guard_jiang.Chat;
import org.guard_jiang.Credential;
import org.guard_jiang.BlockingRecord;
import org.guard_jiang.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 2/3/2017.
 */
public interface Storage {

    @Nonnull
    Set<String> getUserIds() throws IOException;

    @Nullable
    Credential getCredential(@Nonnull String mid) throws IOException;

    void setCredential(@Nonnull String mid, @Nonnull Credential credential) throws IOException;

    @Nonnull
    Map<String, Role> getGroupRoles(@Nonnull String groupId) throws IOException;

    void setGroupRole(@Nonnull String groupId, @Nonnull String userId, @Nonnull Role role) throws IOException;

    void removeGroupRole(@Nonnull String groupId, @Nonnull String userId) throws IOException;

    @Nonnull
    Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException;

    void setGroupBlockingRecord(@Nonnull String groupId, @Nonnull BlockingRecord blockingRecord) throws IOException;

    @Nonnull
    Set<String> getGroupMembersBackup(@Nonnull String groupId) throws IOException;

    void setGroupMembersBackup(@Nonnull String groupId, @Nonnull Set<String> members) throws IOException;

    @Nullable
    GroupMetadata getGroupMetadata(@Nonnull String groupId) throws IOException;

    void setGroupMetadata(@Nonnull String groupId, @Nonnull GroupMetadata meta) throws IOException;

    @Nullable
    Chat getChat(@Nonnull String hostId, @Nonnull String guestId) throws IOException;

    void setChat(@Nonnull Chat chat) throws IOException;
}
