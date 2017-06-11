package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Created by icand on 2017/6/1.
 */
public class GroupRole {
    private long id;
    private final String groupId;
    private final String userId;
    private final Role role;
    private final long licenseId;

    public GroupRole(@Nonnull String groupId, @Nonnull String userId, @Nonnull Role role, long licenseId) {
        this(-1L, groupId, userId, role, licenseId);
    }

    public GroupRole(long id, @Nonnull String groupId, @Nonnull String userId, @Nonnull Role role, long licenseId) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.licenseId = licenseId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public long getLicenseId() {
        return licenseId;
    }
}
