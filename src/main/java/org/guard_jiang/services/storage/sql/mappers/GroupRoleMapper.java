package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.BlockingRecord;
import org.guard_jiang.License;
import org.guard_jiang.Role;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.services.storage.sql.records.GroupRoleRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * MyBatis mappers for manipulating group related operations.
 */
public interface GroupRoleMapper {

    void addGroupRole(
            @Param("groupRole") @Nonnull GroupRoleRecord groupRole);

    void removeGroupRole(
            @Param("id") long id);

    @Nonnull
    List<GroupRoleRecord> getGroupRoles(
            @Param("groupId") @Nonnull String groupId,
            @Param("role") @Nullable Role role,
            @Param("userId") @Nullable String userId,
            @Param("forUpdate") boolean forUpdate);

    @Nonnull
    Collection<BlockingRecord> getGroupBlockingRecords(
            @Param("groupId") @Nonnull String groupId,
            @Param("nowMs") long nowMs);

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
            @Param("licenseId") @Nonnull String licenseId,
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
            @Param("numSupportersAdd") int numSupportersAdd,
            @Param("numAdminsAdd") int numAdminsAdd
    );

    /**
     * Get the set of ID's of groups which a given user have created a role inside.
     *
     * @param userId User's LINE mid.
     * @return Set of group ID's.
     */
    Set<String> getGroupsWithRolesCreatedByUser(
            @Param("userId") @Nonnull String userId
    );
}
