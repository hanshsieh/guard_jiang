package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.BlockingRecord;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by icand on 2017/8/26.
 */
public interface GroupBlockingRecordMapper {

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
}
