package org.guard_jiang.services.storage;

import org.guard_jiang.*;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.AccountsGetter;
import org.guard_jiang.services.storage.sql.GroupMetadata;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

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
    AccountsGetter getGuardAccounts();

    @Nonnull
    AccountCreator createGuardAccount();

    @Nonnull
    GroupRolesGetter getGroupRoles();

    @Nonnull
    GroupRoleCreator createGroupRole();

    @Nonnull
    GroupRoleRemover removeGroupRole();

    /**
     * Get the set of ID's of groups which a given user have created a role inside.
     *
     * @param userId User's LINE mid.
     * @return Set of group ID's.
     */
    @Nonnull
    GroupIdsGetter getGroupIds();

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

    /**
     * Get license with its ID.
     *
     * @param licenseId License ID
     * @return License.
     * @throws IOException IO error occurs.
     * @throws IllegalArgumentException No license with the ID can be found.
     */
    @Nonnull
    License getLicense(@Nonnull String licenseId) throws IOException, IllegalArgumentException;

    void createLicense(@Nonnull License license) throws IOException;
}
