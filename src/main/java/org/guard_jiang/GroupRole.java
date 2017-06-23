package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * It represents a user's role in a group.
 */
public class GroupRole {
    private String id;
    private final String groupId;
    private final String userId;
    private final Role role;
    private final String licenseId;

    public GroupRole(
            @Nullable String id,
            @Nonnull String groupId,
            @Nonnull String userId,
            @Nonnull Role role,
            @Nonnull String licenseId) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.licenseId = licenseId;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Nonnull
    public String getUserId() {
        return userId;
    }

    @Nonnull
    public Role getRole() {
        return role;
    }

    @Nonnull
    public String getLicenseId() {
        return licenseId;
    }
}
