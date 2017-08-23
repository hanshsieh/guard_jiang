package org.guard_jiang.services.storage.sql;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Created by someone on 4/3/2017.
 */
public class GroupMetadata {
    private String groupId;
    private Instant recoveryExpiryTime;
    private Instant membersBackupTime;

    public GroupMetadata(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Nullable
    public Instant getRecoveryExpiryTime() {
        return recoveryExpiryTime;
    }

    @Nullable
    public Instant getMembersBackupTime() {
        return membersBackupTime;
    }

    public void setMembersBackupTime(@Nullable Instant membersBackupTime) {
        this.membersBackupTime = membersBackupTime;
    }

    public void setRecoveryExpiryTime(@Nullable Instant recoveryExpiryTime) {
        this.recoveryExpiryTime = recoveryExpiryTime;
    }
}
