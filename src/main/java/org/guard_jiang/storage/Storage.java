package org.guard_jiang.storage;

import org.guard_jiang.*;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class is used for manipulating the storage.
 */
public interface Storage {

    @Nonnull
    List<AccountData> getGuardAccounts(boolean withCredential);

    void updateGuardAccount(@Nonnull AccountData accountData) throws IOException;

    @Nonnull
    List<GroupRole> getRolesOfGroup(@Nonnull String groupId, @Nullable Role role) throws IOException;

    @Nullable
    GroupRole getGroupRoleOfUser(@Nonnull String groupId, @Nonnull String userId) throws IOException;

    /**
     *
     * @param groupRole
     * @throws IOException
     * @throws IllegalArgumentException Exceeding maximum number of defenders or supporters.
     */
    void addGroupRole(@Nonnull GroupRole groupRole)
            throws IOException, IllegalArgumentException;

    void removeGroupRole(long id) throws IOException;

    @Nonnull
    Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException;

    void setGroupBlockingRecord(@Nonnull BlockingRecord blockingRecord) throws IOException;

    void removeGroupBlockingRecord(@Nonnull String groupId, @Nonnull String userId) throws IOException;

    @Nonnull
    Set<String> getGroupMembersBackup(@Nonnull String groupId) throws IOException;

    void setGroupMembersBackup(@Nonnull String groupId, @Nonnull Set<String> members) throws IOException;

    @Nullable
    GroupMetadata getGroupMetadata(@Nonnull String groupId) throws IOException;

    void setGroupMetadata(@Nonnull GroupMetadata meta) throws IOException;

    @Nullable
    Chat getChat(@Nonnull String hostId, @Nonnull String guestId, @Nonnull ChatEnv env) throws IOException;

    void setChat(@Nonnull Chat chat) throws IOException;

    @Nonnull
    List<License> getLicensesOfUser(@Nonnull String userId) throws IOException;

    @Nonnull
    License getLicense(long licenseId) throws IOException;

    void createLicense(@Nonnull License license) throws IOException;
}
