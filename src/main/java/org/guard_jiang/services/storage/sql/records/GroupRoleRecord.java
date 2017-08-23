package org.guard_jiang.services.storage.sql.records;

import org.guard_jiang.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Group role.
 */
public class GroupRoleRecord {
    private long id = -1;
    private String groupId;
    private String userId;
    private Role role;
    private long licenseId = -1;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public String getGroupId() {
        return groupId;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    @Nullable
    public Role getRole() {
        return role;
    }

    public long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(long licenseId) {
        this.licenseId = licenseId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
