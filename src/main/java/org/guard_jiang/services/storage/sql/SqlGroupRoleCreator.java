package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.GroupRole;
import org.guard_jiang.GroupRoleCreator;
import org.guard_jiang.License;
import org.guard_jiang.Role;
import org.guard_jiang.exception.ConflictException;
import org.guard_jiang.services.storage.sql.mappers.SqlStorageMapper;
import org.guard_jiang.services.storage.sql.records.GroupRoleRecord;
import org.guard_jiang.services.storage.sql.records.LicenseRecord;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/20.
 */
public class SqlGroupRoleCreator implements GroupRoleCreator {

    private final SqlSessionFactory sqlSessionFactory;
    private final GroupRoleRecord groupRoleRecord = new GroupRoleRecord();
    private int defendersAdd = 0;
    private int supportersAdd = 0;
    private int adminsAdd = 0;

    public SqlGroupRoleCreator(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public GroupRoleCreator withGroupId(@Nonnull String groupId) {
        groupRoleRecord.setGroupId(groupId);
        return this;
    }

    @Nonnull
    @Override
    public GroupRoleCreator withUserId(@Nonnull String userId) {
        groupRoleRecord.setUserId(userId);
        return this;
    }

    @Nonnull
    @Override
    public GroupRoleCreator withRole(@Nonnull Role role) {
        groupRoleRecord.setRole(role);
        return this;
    }

    @Nonnull
    @Override
    public GroupRoleCreator withLicenseId(@Nonnull String licenseId) {
        groupRoleRecord.setLicenseId(Long.parseLong(licenseId));
        return this;
    }

    @Nonnull
    @Override
    public GroupRole create() throws IllegalArgumentException, ConflictException, IOException {
        calLicenseUsageUpdate();
        try (SqlSession session = sqlSessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            LicenseRecord license = getLicense(mapper);
            updateLicenseUsage(mapper, license);
            mapper.addGroupRole(groupRoleRecord);

            // The ID of the group role should have been set by MyBatis
            Validate.notNull(groupRoleRecord.getId());

            session.commit();
        } catch (PersistenceException ex) {
            throw new ConflictException("The user already has a role in the group", ex);
        }

        return new SqlGroupRole(groupRoleRecord);
    }

    @Nonnull
    private LicenseRecord getLicense(@Nonnull SqlStorageMapper mapper) {
        LicenseRecord license = mapper.getLicense(groupRoleRecord.getLicenseId(), true);
        if (license == null) {
            throw new IllegalArgumentException("No license is found with ID " + groupRoleRecord.getLicenseId());
        }
        Validate.isTrue(license.getNumAdmins() >= 0,
                "Number is admins of the license is negative");
        Validate.isTrue(license.getNumSupporters() >= 0,
                "Number is supporters of the license is negative");
        Validate.isTrue(license.getNumDefenders() >= 0,
                "Number is defenders of the license is negative");
        return license;
    }

    private void calLicenseUsageUpdate() {
        defendersAdd = 0;
        supportersAdd = 0;
        adminsAdd = 0;
        Role role = groupRoleRecord.getRole();
        if(Role.DEFENDER.equals(role)) {
            defendersAdd++;
        } else if (Role.SUPPORTER.equals(role)) {
            supportersAdd++;
        } else if (Role.ADMIN.equals(role)) {
            adminsAdd++;
        } else {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }

    private void updateLicenseUsage(@Nonnull SqlStorageMapper mapper, @Nonnull LicenseRecord license) {
        license.setNumDefenders(license.getNumDefenders() + defendersAdd);
        license.setNumSupporters(license.getNumSupporters() + supportersAdd);
        license.setNumAdmins(license.getNumAdmins() + adminsAdd);

        Validate.isTrue(license.getNumDefenders() > license.getMaxDefenders(),
                "Exceeding maximum number of defenders of the license");
        Validate.isTrue(license.getMaxSupporters() < license.getNumSupporters(),
                "Exceeding maximum number of supporters of the license");
        Validate.isTrue(license.getMaxAdmins() < license.getNumAdmins(),
                "Exceeding maximum number of admins of the license");
        mapper.updateLicense(license);
    }
}
