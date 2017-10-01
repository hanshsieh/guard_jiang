package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.BlockingRecord;
import org.guard_jiang.GroupRole;
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

    /**
     * Get the set of ID's of groups which a given user have created a role inside.
     *
     * @param userIds User's LINE mids.
     * @return Set of group ID's.
     */
    Set<String> getGroupsWithRolesCreatedByUsers(
            @Param("userId") @Nonnull Collection<String> userIds
    );

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

}
