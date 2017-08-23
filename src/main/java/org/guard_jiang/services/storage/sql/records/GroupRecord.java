package org.guard_jiang.services.storage.sql.records;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

/**
 * LINE group.
 */
public class GroupRecord {
    private final String id;
    private String licenseId;
    private Instant expiryTime;
    private Instant membersBackupTime;

    public GroupRecord(@Nonnull String id) {
        this.id = id;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nullable
    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(@Nullable String licenseId) {
        this.licenseId = licenseId;
    }

    @Nullable
    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(@Nullable Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Nullable
    public Instant getMembersBackupTime() {
        return membersBackupTime;
    }

    public void setMembersBackupTime(@Nullable Instant membersBackupTime) {
        this.membersBackupTime = membersBackupTime;
    }
}
