package org.guard_jiang.storage;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
 * CREATE TABLE "user" (
 *      id VARCHAR PRIMARY KEY NOT NULL,
 *      email VARCHAR NOT NULL,
 *      password VARCHAR NOT NULL,
 *      certificate VARCHAR NOT NULL,
 *      auth_token VARCHAR DEFAULT NULL);
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
 * CREATE UNIQUE INDEX group_member_group_id_user_id_u_idx ON group_member_backup(group_id, user_id);
 */
public class SqlStorage implements Storage {

    private static final String CONFIG_FILE = "mybatis.xml";

    private final SqlSessionFactory sessionFactory;

    public SqlStorage() throws IOException {
        try (Reader configReader = Resources.getResourceAsReader(CONFIG_FILE)) {
            sessionFactory = new SqlSessionFactoryBuilder().build(configReader);
        }
    }

    public static void main(String[] args) throws Throwable {
        SqlStorage storage = new SqlStorage();
        Config config = ConfigFactory.load("config.conf");
        /*
        List<? extends Config> accountsConf = config.getConfigList("accounts");
        for (Config accountConf : accountsConf) {
            String mid = accountConf.getString("mid");
            String email = accountConf.getString("email");
            String password = accountConf.getString("password");
            String certificate = accountConf.getString("certificate");
            String authToken = accountConf.getString("authToken");
            Credential credential = new Credential();
            credential.setEmail(email);
            credential.setPassword(password);
            credential.setCertificate(certificate);
            credential.setAuthToken(authToken);
            storage.setCredential(mid, credential);
        }
*/


        List<? extends Config> groupsConf = config.getConfigList("groups");
        for (Config groupConf : groupsConf) {
            String groupId = groupConf.getString("id");
            for (String userId : groupConf.getStringList("defenders")) {
                storage.setGroupRole(
                        groupId, userId, Role.DEFENDER);
            }
            for (String userId : groupConf.getStringList("supporters")) {
                storage.setGroupRole(
                        groupId, userId, Role.SUPPORTER);
            }
            for (String userId : groupConf.getStringList("admins")) {
                storage.setGroupRole(
                        groupId, userId, Role.ADMIN);
            }
            Map<String, Role> roles = storage.getGroupRoles(groupId);
            System.out.println("Role for group " + groupId + ": " + roles);
        }
    }

    public SqlStorage(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public Set<String> getUserIds() throws IOException {
        try (SqlSession session = sessionFactory.openSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            return mapper.getUserIds();
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
}
