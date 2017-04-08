package org.guard_jiang.storage;

import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Created by someone on 4/3/2017.
 */
public class GroupMetadata {
    private Instant recoveryExpiryTime;
    private Instant membersBackupTime;

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
