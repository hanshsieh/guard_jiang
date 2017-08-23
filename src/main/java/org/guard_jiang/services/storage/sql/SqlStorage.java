package org.guard_jiang.services.storage.sql;

import com.typesafe.config.Config;
import org.apache.commons.lang3.Validate;
import org.guard_jiang.*;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.AccountCreator;
import org.guard_jiang.services.storage.Storage;
import org.guard_jiang.services.storage.sql.mappers.SqlStorageMapper;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class SqlStorage implements Storage {

    private final SqlSessionFactory sessionFactory;

    private final int partition;

    public SqlStorage(
            @Nonnull Config config,
            @Nonnull SqlSessionFactory sqlSessionFactory) throws IOException {
        this.partition = config.getInt("partition");
        this.sessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public AccountsGetter getGuardAccounts() {
        return new SqlAccountsGetter(partition, sessionFactory);
    }

    @Nonnull
    @Override
    public AccountCreator createGuardAccount() {
        return new SqlAccountCreator(sessionFactory);
    }

    @Nonnull
    @Override
    public GroupRolesGetter getGroupRoles() {
        return new SqlGroupRolesGetter(sessionFactory);
    }

    @Override
    public GroupRoleCreator createGroupRole() {
        return new SqlGroupRoleCreator(sessionFactory);
    }

    @Nonnull
    public GroupRoleRemover removeGroupRole() {
        return new SqlGroupRoleRemover(sessionFactory);
    }

    @Nonnull
    @Override
    public Set<String> getGroupsWithRolesCreatedByUser(@Nonnull String userId) {
        try (SqlSession session = sessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupsWithRolesCreatedByUser(userId);
        }
    }

    @Nonnull
    @Override
    public Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openReadSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupBlockingRecords(groupId, System.currentTimeMillis());
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
