package org.guard_jiang.storage;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.*;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by someone on 4/1/2017.
 */
public interface SqlStorageMapper {

    @Nonnull
    List<AccountData> getGuardAccounts(
            @Param("partition") int partition,
            @Param("withCredential") boolean withCredential);

    void updateGuardAccount(
            @Param("account") @Nonnull AccountData accountData);

    void addGroupRole(
            @Param("groupRole") @Nonnull GroupRole groupRole);

    @Nonnull
    List<GroupRole> getRolesOfGroup(
            @Param("groupId") @Nonnull String groupId,
            @Param("role") @Nullable Role role);

    @Nullable
    GroupRole getGroupRoleOfUser(
            @Param("groupId") @Nonnull String groupId, @Param("userId") @Nonnull String userId);

    @Nullable
    GroupRole getGroupRole(
            @Param("id") long groupRoleId,
            @Param("forUpdate") boolean forUpdate);

    void removeGroupRole(
            @Param("id") long id);

    void setGroupMetadata(
            @Param("metadata") @Nonnull GroupMetadata metadata);

    @Nullable
    GroupMetadata getGroupMetadata(@Param("groupId") @Nonnull String groupId);

    @Nonnull
    Collection<BlockingRecord> getGroupBlockingRecords(
            @Param("groupId") @Nonnull String groupId);

    void setGroupBlockingRecord(
            @Param("record") @Nonnull BlockingRecord record);

    void removeGroupBlockingRecord(
            @Param("groupId") @Nonnull String groupId,
            @Param("userId") @Nonnull String userId
    );

    @Nonnull
    Set<String> getGroupMembersBackup(
            @Param("groupId") @Nonnull String groupId
    );

    @Nonnull
    void addGroupMembersBackup(
            @Param("groupId") @Nonnull String groupId,
            @Param("userId") @Nonnull String userId
    );

    @Nonnull
    void clearGroupMemberBackup(
            @Param("groupId") @Nonnull String groupId
    );

    @Nonnull
    Chat getChat(
            @Param("guardId") @Nonnull String guardId,
            @Param("userId") @Nonnull String userId,
            @Param("chatEnv") @Nonnull ChatEnv chatEnv
    );

    void setChat(
            @Param("chat") @Nonnull Chat chat
    );

    @Nonnull
    List<License> getLicensesOfUser(
            @Param("userId") @Nonnull String userId);

    @Nullable
    License getLicense(
            @Param("licenseId") long licenseId,
            @Param("forUpdate") boolean forUpdate);

    void createLicense(
            @Param("license") @Nonnull License license
    );

    void updateLicense(
            @Param("license") @Nonnull License license
    );

    void updateLicenseUsage(
            @Param("licenseId") long licenseId,
            @Param("numDefendersAdd") int numDefendersAdd,
            @Param("numSupportersAdd") int numSupportersAdd
    );
}
