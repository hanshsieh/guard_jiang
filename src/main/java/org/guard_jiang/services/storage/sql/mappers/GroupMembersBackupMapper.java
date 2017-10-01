package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by icand on 2017/8/26.
 */
public interface GroupMembersBackupMapper {

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
}
