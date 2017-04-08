package org.guard_jiang.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.guard_jiang.Credential;
import org.guard_jiang.BlockingRecord;
import org.guard_jiang.Relation;
import org.guard_jiang.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 2/3/2017.
 */
public class InMemoryStorage implements Storage {

    @Nonnull
    private final Map<String, Credential> credentials = new HashMap<>();

    @Nonnull
    private final Map<Relation, Role> roles = new HashMap<>();

    @Nonnull
    private final Map<String, Map<String, BlockingRecord>> groupBlockingRecords = new HashMap<>();

    @Nonnull
    private final Map<String, Set<String>> groupMembersBackup = new HashMap<>();

    @Nonnull
    private final Map<String, GroupMetadata> groupsMeta = new HashMap<>();

    @Nonnull
    @Override
    public Set<String> getUserIds() throws IOException {
        synchronized (credentials) {
            return ImmutableSet.copyOf(credentials.keySet());
        }
    }

    @Nullable
    @Override
    public Credential getCredential(@Nonnull String mid) throws IOException {
        synchronized (credentials) {
            return credentials.get(mid);
        }
    }

    @Override
    public void setCredential(@Nonnull String mid, @Nonnull Credential credential) throws IOException {
        synchronized (credentials) {
            credentials.put(mid, credential);
        }
    }

    @Nonnull
    @Override
    public Map<String, Role> getGroupRoles(@Nonnull String groupId) throws IOException {
        synchronized (roles) {
            return roles.entrySet().stream()
                    .filter(entry -> groupId.equals(entry.getKey().getGroupId()))
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getUserId(),
                            Map.Entry::getValue
                    ));
        }
    }

    @Override
    public void setGroupRole(@Nonnull String groupId, @Nonnull String userId, @Nonnull Role role) throws IOException {
        synchronized (roles) {
            roles.put(new Relation(userId, groupId), role);
        }
    }

    @Override
    public void removeGroupRole(@Nonnull String groupId, @Nonnull String userId) throws IOException {
        synchronized (roles) {
            roles.remove(new Relation(userId, groupId));
        }
    }

    @Nonnull
    @Override
    public Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) {
        synchronized (groupBlockingRecords) {
            Map<String, BlockingRecord> records = groupBlockingRecords.get(groupId);
            if (records == null) {
                return Collections.emptySet();
            }
            Iterator<Map.Entry<String, BlockingRecord>> itr = records.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, BlockingRecord> entry = itr.next();
                if (entry.getValue().isExpired()) {
                    itr.remove();
                }
            }
            return ImmutableList.copyOf(records.values());
        }
    }

    @Override
    public void setGroupBlockingRecord(@Nonnull String groupId, @Nonnull BlockingRecord blockingRecord) {
        synchronized (groupBlockingRecords) {
            Map<String, BlockingRecord> records = groupBlockingRecords.get(groupId);
            if (records == null) {
                records = new HashMap<>();
                groupBlockingRecords.put(groupId, records);
            }
            records.put(blockingRecord.getUserId(), blockingRecord);
        }
    }

    @Nonnull
    @Override
    public Set<String> getGroupMembersBackup(@Nonnull String groupId) throws IOException {
        synchronized (groupMembersBackup) {
            return groupMembersBackup.get(groupId);
        }
    }

    @Override
    public void setGroupMembersBackup(@Nonnull String groupId, @Nonnull Set<String> members) throws IOException {
        synchronized (groupMembersBackup) {
            groupMembersBackup.put(groupId, ImmutableSet.copyOf(members));
        }
    }

    @Nullable
    @Override
    public GroupMetadata getGroupMetadata(@Nonnull String groupId) throws IOException {
        synchronized (groupsMeta) {
            return groupsMeta.get(groupId);
        }
    }

    @Override
    public void setGroupMetadata(@Nonnull String groupId, @Nonnull GroupMetadata meta) throws IOException {
        synchronized (groupsMeta) {
            groupsMeta.put(groupId, meta);
        }
    }
}
