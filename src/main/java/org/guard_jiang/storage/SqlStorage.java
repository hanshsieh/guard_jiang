package org.guard_jiang.storage;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.guard_jiang.Chat;
import org.guard_jiang.Credential;
import org.guard_jiang.BlockingRecord;
import org.guard_jiang.Role;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.guard_jiang.UserRole;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * CREATE TABLE "user2" (
 *      id VARCHAR PRIMARY KEY NOT NULL,
 *      email VARCHAR NOT NULL,
 *      password VARCHAR NOT NULL,
 *      certificate VARCHAR NOT NULL,
 *      auth_token VARCHAR DEFAULT NULL,
 *      env TINYINT NOT NULL);
 *
 * CREATE TABLE "group" (
 *      id VARCHAR PRIMARY KEY NOT NULL,
 *      recovery_expiry_ts BIGINT DEFAULT NULL,
 *      members_backup_ts BIGINT DEFAULT NULL
 * );
 *
 * CREATE TABLE "role" (
 *      group_id VARCHAR NOT NULL,
 *      user_id VARCHAR NOT NULL, -- It may reference a user not in the "user" table
 *      role UNSIGNED TINYINT NOT NULL -- 0: defender, 1: supporter, 2: admin
 * );
 *
 * CREATE UNIQUE INDEX role_group_id_user_id_u_idx ON role(group_id, user_id);
 *
 * CREATE TABLE "group_blocking_record" (
 *      group_id VARCHAR NOT NULL,
 *      user_id VARCHAR NOT NULL,
 *      expiry_ts BIGINT DEFAULT NULL);
 *
 * CREATE UNIQUE INDEX group_blocking_record_group_id_user_id_u_idx ON group_blocking_record(user_id, group_id);
 *
 * CREATE TABLE "group_member_backup"(
 *      group_id VARCHAR NOT NULL,
 *      user_id VARCHAR NOT NULL
 * );
 *
 * CREATE UNIQUE INDEX group_member_backup_group_id_user_id_u_idx ON group_member_backup(group_id, user_id);
 *
 * CREATE INDEX group_member_backup_group_id ON group_member_backup(group_id);
 *
 * CREATE TABLE "chat" (
 *      host_id VARCHAR NOT NULL REFERENCES "user"(id),
 *      guest_id VARCHAR NOT NULL,
 *      status INT NOT NULL,
 *      metadata VARCHAR NOT NULL
 * );
 *
 * CREATE UNIQUE INDEX chat_host_id_guest_id_u_idx ON "chat"(host_id, guest_id);
 *
 * CREATE INDEX chat_host_id ON "chat"(host_id);
 */
public class SqlStorage implements Storage {

    private static final String CONFIG_FILE = "mybatis.xml";

    private final SqlSessionFactory sessionFactory;

    private final StorageEnv env;

    public SqlStorage(@Nonnull StorageEnv env) throws IOException {
        this.env = env;
        try (Reader configReader = Resources.getResourceAsReader(CONFIG_FILE)) {
            sessionFactory = new SqlSessionFactoryBuilder().build(configReader);
        }
    }

    public SqlStorage(@Nonnull StorageEnv env, @Nonnull SqlSessionFactory sqlSessionFactory) {
        this.env = env;
        this.sessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public Set<String> getUserIds() throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getUserIds(env);
        }
    }

    @Nullable
    @Override
    public Credential getCredential(@Nonnull String mid) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getCredential(mid);
        }
    }

    @Override
    public void setCredential(@Nonnull String mid, @Nonnull Credential credential) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setCredential(mid, credential);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Map<String, Role> getGroupRoles(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            List<UserRole> roles = mapper.getGroupRoles(groupId);
            return roles
                    .stream()
                    .collect(Collectors.toMap(UserRole::getUserId, UserRole::getRole));
        }
    }

    @Override
    public void setGroupRole(
            @Nonnull String groupId,
            @Nonnull String userId,
            @Nonnull Role role) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setGroupRole(groupId, userId, role);
            session.commit();
        }
    }

    @Override
    public void removeGroupRole(@Nonnull String groupId, @Nonnull String userId) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.removeGroupRole(groupId, userId);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupBlockingRecords(groupId);
        }
    }

    @Override
    public void setGroupBlockingRecord(@Nonnull String groupId, @Nonnull BlockingRecord blockingRecord) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setGroupBlockingRecord(groupId, blockingRecord);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Set<String> getGroupMembersBackup(@Nonnull String groupId) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupMembersBackup(groupId);
        }
    }

    @Override
    public void setGroupMembersBackup(@Nonnull String groupId, @Nonnull Set<String> members) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
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
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getGroupMetadata(groupId);
        }
    }

    @Override
    public void setGroupMetadata(@Nonnull String groupId, @Nonnull GroupMetadata meta) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setGroupMetadata(groupId, meta);
            session.commit();
        }
    }

    @Nonnull
    @Override
    public Chat getChat(@Nonnull String hostId, @Nonnull String guestId) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getChat(hostId, guestId);
        }
    }

    @Override
    public void setChat(@Nonnull Chat chat) throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.setChat(chat);
            session.commit();
        }
    }
}
