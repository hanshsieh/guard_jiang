package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.services.storage.sql.records.GroupRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * MyBatis mappers for manipulating group related operations.
 */
public interface GroupMapper {

    void registerGroup(
            @Param("groupId") @Nonnull String groupId
    );

    @Nullable
    GroupRecord getGroup(
            @Param("groupId") @Nonnull String groupId
    );

    int updateGroup(
            @Param("group") @Nonnull GroupRecord group);
}
