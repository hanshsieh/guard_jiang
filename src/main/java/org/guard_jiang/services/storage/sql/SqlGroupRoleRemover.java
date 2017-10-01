package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.GroupRole;
import org.guard_jiang.GroupRoleRemover;
import org.guard_jiang.Role;
import org.guard_jiang.services.storage.sql.mappers.GroupRoleMapper;
import org.guard_jiang.services.storage.sql.mappers.LicenseMapper;
import org.guard_jiang.services.storage.sql.mappers.SqlStorageMapper;
import org.guard_jiang.services.storage.sql.records.GroupRoleRecord;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by icand on 2017/8/20.
 */
public class SqlGroupRoleRemover implements GroupRoleRemover {

    private SqlSessionFactory sqlSessionFactory;
    private int numDefendersAdd = 0, numSupportersAdd = 0, numAdminsAdd = 0;
    private String groupId;
    private String userId;

    public SqlGroupRoleRemover(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public GroupRoleRemover withGroupId(@Nonnull String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Nonnull
    @Override
    public GroupRoleRemover withUserId(@Nonnull String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public boolean remove() throws IOException {
        try (SqlSession session = sqlSessionFactory.openWriteSession()) {
            GroupRoleMapper groupRoleMapper = session.getMapper(GroupRoleMapper.class);
            List<GroupRoleRecord> groupRoles = groupRoleMapper.getGroupRoles(groupId, null, userId, true);
            if (groupRoles.isEmpty()) {
                return false;
            }
            Validate.isTrue(groupRoles.size() == 1);
            GroupRoleRecord groupRole = groupRoles.get(0);

            Role role = groupRole.getRole();
            Validate.notNull(role);

            calLicenseUsageUpdateForRole(role);

            updateLicenseUsage(groupRole.getLicenseId(), session);
            groupRoleMapper.removeGroupRole(groupRole.getId());
            session.commit();
        }
        return true;
    }

    private void updateLicenseUsage(long licenseId, @Nonnull SqlSession session) {
        LicenseMapper licenseMapper = session.getMapper(LicenseMapper.class);
        licenseMapper.updateLicenseUsage(
                licenseId,
                numDefendersAdd,
                numSupportersAdd,
                numAdminsAdd);
    }

    private void calLicenseUsageUpdateForRole(@Nonnull Role role) {
        numDefendersAdd = 0;
        numSupportersAdd = 0;
        numAdminsAdd = 0;
        if (Role.DEFENDER.equals(role)) {
            numDefendersAdd--;
        } else if (Role.SUPPORTER.equals(role)) {
            numSupportersAdd--;
        } else if (Role.ADMIN.equals(role)) {
            numAdminsAdd--;
        } else {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
