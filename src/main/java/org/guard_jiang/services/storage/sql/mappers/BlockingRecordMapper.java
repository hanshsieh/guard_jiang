package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.License;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.services.storage.sql.records.BlockingRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * MyBatis mappers for manipulating group blocking record related operations.
 */
public interface BlockingRecordMapper {

    @Nonnull
    Collection<BlockingRecord> getBlockingRecords(
            @Param("groupId") @Nonnull String groupId,
            @Param("nowTime") @Nullable Instant nowTime);

    void putBlockingRecord(
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
            @Param("licenseId") @Nonnull String licenseId,
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
