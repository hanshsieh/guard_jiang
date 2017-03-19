package com.handoitadsf.line.group_guard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 2/3/2017.
 */
public class InMemoryStorage implements Storage {

    @Nonnull
    private final Map<String, AccountCredential> credentials = new HashMap<>();

    @Nonnull
    private final Map<Relation, Role> roles = new HashMap<>();

    @Nonnull
    private final Map<String, Set<String>> groupAdmins = new HashMap<>();

    @Nonnull
    private final Map<String, Map<String, BlockingRecord>> groupBlockingRecords = new HashMap<>();

    @Nonnull
    @Override
    public Set<String> getAccountIds() throws IOException {
        synchronized (credentials) {
            return ImmutableSet.copyOf(credentials.keySet());
        }
    }

    @Nullable
    @Override
    public AccountCredential getAccountCredential(@Nonnull String mid) throws IOException {
        synchronized (credentials) {
            return credentials.get(mid);
        }
    }

    @Override
    public void setAccountCredential(@Nonnull String mid, @Nonnull AccountCredential credential) throws IOException {
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

    @Nonnull
    @Override
    public Set<String> getGroupAdminIds(@Nonnull String groupId) {
        synchronized (groupAdmins) {
            Set<String> admins = groupAdmins.get(groupId);
            if (admins == null) {
                return Collections.emptySet();
            }
            return ImmutableSet.copyOf(admins);
        }
    }

    @Override
    public void setGroupAdmins(@Nonnull String groupId, @Nonnull Set<String> admins) {
        synchronized (groupAdmins) {
            groupAdmins.put(groupId, admins);
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
            return ImmutableList.copyOf(records.values());
        }
    }

    @Override
    public void putGroupBlockingRecord(@Nonnull String groupId, @Nonnull BlockingRecord blockingRecord) {
        synchronized (groupBlockingRecords) {
            Map<String, BlockingRecord> records = groupBlockingRecords.get(groupId);
            if (records == null) {
                records = new HashMap<>();
                groupBlockingRecords.put(groupId, records);
            }
            records.put(blockingRecord.getAccountId(), blockingRecord);
        }
    }

    public void addRole(Relation relation, Role role) {
        synchronized (roles) {
            roles.put(relation, role);
        }
    }
}
