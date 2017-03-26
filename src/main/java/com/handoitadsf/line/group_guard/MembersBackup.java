package com.handoitadsf.line.group_guard;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Set;

/**
 * Created by someone on 3/25/2017.
 */
public class MembersBackup {
    private final Instant backupTime;
    private final Set<String> members;

    public MembersBackup(@Nonnull Set<String> members) {
        this.members = ImmutableSet.copyOf(members);
        this.backupTime = Instant.now();
    }

    public Instant getBackupTime() {
        return backupTime;
    }

    public Set<String> getMembers() {
        return members;
    }
}
