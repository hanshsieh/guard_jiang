package org.guard_jiang.storage;

import com.typesafe.config.Config;
import org.apache.commons.lang3.Validate;
import org.guard_jiang.*;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.apache.ibatis.session.SqlSession;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class SqlStorage implements Storage {

    private final MyBatisSqlSessionFactory sessionFactory;

    private final int partition;

    public SqlStorage(
            @Nonnull Config config,
            @Nonnull MyBatisSqlSessionFactory sqlSessionFactory) throws IOException {
        this.partition = config.getInt("partition");
        this.sessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public List<AccountData> getGuardAccounts(boolean withCredential) {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            List<AccountData> accountsData = mapper.getGuardAccounts(partition, withCredential);
            if (!withCredential) {
                for (AccountData accountData : accountsData) {
                    accountData.setCredential(null);
                }
            }
            return accountsData;
        }
    }

    @Override
    public void createGuardAccount(@Nonnull AccountData accountData) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.createGuardAccount(accountData);
            session.commit();
        }
    }

    @Override
    public void updateGuardAccount(@Nonnull AccountData accountData) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            int nUpdated = mapper.updateGuardAccount(accountData);
            if (nUpdated <= 0) {
                throw new IOException("The account doesn't exist");
            }
            session.commit();
        }
    }

    @Nonnull
    @Override
    public List<GroupRole> getRolesOfGroup(
            @Nonnull String groupId,
            @Nullable Role role) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getRolesOfGroup(groupId, role);
        }
    }

    @Nullable
    @Override
    public GroupRole getGroupRoleOfUser(@Nonnull String groupId, @Nonnull String userId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupRoleOfUser(groupId, userId);
        }
    }

    @Override
    public void addGroupRole(@Nonnull GroupRole groupRole) throws IOException {
        int defendersAdd = 0;
        int supportersAdd = 0;
        Role role = groupRole.getRole();
        if(Role.DEFENDER.equals(role)) {
            defendersAdd++;
        } else if (Role.SUPPORTER.equals(role)) {
            supportersAdd++;
        }
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            if(defendersAdd != 0 || supportersAdd != 0) {
                License license = mapper.getLicense(groupRole.getLicenseId(), true);
                if (license == null) {
                    throw new IOException("No license is found with ID " + groupRole.getLicenseId());
                }
                license.setNumDefenders(license.getNumDefenders() + defendersAdd);
                license.setNumSupporters(license.getNumSupporters() + supportersAdd);
                updateLicense(mapper, license);
            }
            mapper.addGroupRole(groupRole);
            session.commit();
        }
    }

    private void updateLicense(@Nonnull SqlStorageMapper mapper, @Nonnull License license) {
        if (license.getNumDefenders() > license.getMaxDefenders()) {
            throw new IllegalArgumentException("Exceeding maximum number of defenders of the license");
        }
        if (license.getMaxSupporters() < license.getNumSupporters()) {
            throw new IllegalArgumentException("Exceeding maximum number of supporters of the license");
        }
        if (license.getMaxDefenders() < 0 || license.getNumDefenders() < 0 ||
            license.getMaxSupporters() < 0 || license.getNumSupporters() < 0) {
            throw new IllegalArgumentException("Negative number of defenders/supporters isn't allowed");
        }
        mapper.updateLicense(license);
    }

    @Override
    public void removeGroupRole(long id) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            GroupRole groupRole = mapper.getGroupRole(id, true);
            if (groupRole == null) {
                throw new IllegalArgumentException("Group role with ID " + id + " cannot be found");
            }
            Role role = groupRole.getRole();
            int numDefendersAdd = 0, numSupportersAdd = 0;
            if (Role.DEFENDER.equals(role)) {
                numDefendersAdd--;
            } else if (Role.SUPPORTER.equals(role)) {
                numSupportersAdd--;
            }
            if (numDefendersAdd != 0 || numSupportersAdd != 0) {
                mapper.updateLicenseUsage(groupRole.getLicenseId(), numDefendersAdd, numSupportersAdd);
            }
            mapper.removeGroupRole(id);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupBlockingRecords(groupId);
        }
    }

    @Override
    public void setGroupBlockingRecord(@Nonnull BlockingRecord blockingRecord) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setGroupBlockingRecord(blockingRecord);
            session.commit();
        }
    }

    @Override
    public void removeGroupBlockingRecord(@Nonnull String groupId, @Nonnull String userId) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.removeGroupBlockingRecord(groupId, userId);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Set<String> getGroupMembersBackup(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupMembersBackup(groupId);
        }
    }

    @Override
    public void setGroupMembersBackup(@Nonnull String groupId, @Nonnull Set<String> members) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);

            // Clear the old members
            mapper.clearGroupMemberBackup(groupId);

            // Add new members
            for (String member : members) {
                mapper.addGroupMembersBackup(groupId, member);
            }
            session.commit();
        }
    }

    @Override
    public GroupMetadata getGroupMetadata(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupMetadata(groupId);
        }
    }

    @Override
    public void setGroupMetadata(@Nonnull GroupMetadata meta) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setGroupMetadata(meta);
            session.commit();
        }
    }

    @Nullable
    public Chat getChat(
            @Nonnull String guardId,
            @Nonnull String userId,
            @Nonnull ChatEnv env) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getChat(guardId, userId, env);
        }
    }

    @Override
    public void setChat(@Nonnull Chat chat) throws IOException {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setChat(chat);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public List<License> getLicensesOfUser(@Nonnull String userId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getLicensesOfUser(userId);
        }
    }

    @Nonnull
    @Override
    public License getLicense(@Nonnull String licenseId) throws IOException, IllegalArgumentException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            License license = mapper.getLicense(licenseId, false);
            if (license == null) {
                throw new IllegalArgumentException("No license with the given ID could be found. id: " + licenseId);
            }
            return license;
        }
    }

    @Override
    public void createLicense(@Nonnull License license) throws IOException {
        Validate.isTrue(
                license.getNumDefenders() == 0,
                "License must have 0 defenders initially");
        Validate.isTrue(
                license.getNumSupporters() == 0,
                "License must have 0 supporter initially");
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.createLicense(license);
            session.commit();
        }
    }

}
