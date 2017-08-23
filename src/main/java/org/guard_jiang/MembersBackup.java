package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * This class represents the backup of the members of a group.
 */
public class MembersBackup {

    private final String groupId;
    private final Set<String> members;
    private final Instant backupTime;
    private final Instant recoveryExpiryTime;
    public MembersBackup(
            @Nonnull String groupId,
            @Nonnull Instant backupTime,
            @Nonnull Instant recoveryExpiryTime,
            @Nonnull Set<String> members) {
        this.groupId = groupId;
        this.backupTime = backupTime;
        this.recoveryExpiryTime = recoveryExpiryTime;
        this.members = members;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Nonnull
    public Set<String> getMembers() {
        return members;
    }

    @Nonnull
    public Instant getBackupTime() {
        return backupTime;
    }

    @Nonnull
    public Instant getRecoveryExpiryTime() {
        return recoveryExpiryTime;
    }

    public void updateRecoveryExpiryTime(@Nonnull String time) throws IOException {

    }
}
