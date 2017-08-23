package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.guard_jiang.GroupRole;
import org.guard_jiang.Role;
import org.guard_jiang.services.storage.sql.records.GroupRoleRecord;

import javax.annotation.Nonnull;

/**
 * Created by icand on 2017/8/20.
 */
public class SqlGroupRole implements GroupRole {

    private final GroupRoleRecord groupRoleRecord;

    public SqlGroupRole(@Nonnull GroupRoleRecord groupRoleRecord) {
        Validate.notNull(groupRoleRecord.getGroupId());
        Validate.notNull(groupRoleRecord.getUserId());
        Validate.notNull(groupRoleRecord.getRole());
        Validate.isTrue(groupRoleRecord.getId() >= 0);
        Validate.isTrue(groupRoleRecord.getLicenseId() >= 0);
        this.groupRoleRecord = groupRoleRecord;
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(groupRoleRecord.getId());
    }

    @Nonnull
    @Override
    public String getGroupId() {
        assert groupRoleRecord.getGroupId() != null;
        return groupRoleRecord.getGroupId();
    }

    @Nonnull
    @Override
    public String getUserId() {
        assert groupRoleRecord.getUserId() != null;
        return groupRoleRecord.getUserId();
    }

    @Nonnull
    @Override
    public Role getRole() {
        assert groupRoleRecord.getRole() != null;
        return groupRoleRecord.getRole();
    }

    @Nonnull
    @Override
    public String getLicenseId() {
        return String.valueOf(groupRoleRecord.getLicenseId());
    }
}
