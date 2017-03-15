package com.handoitadsf.line.group_guard;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 1/31/2017.
 */
public class GroupProfile {
    private final String groupId;
    private final Set<String> adminIds = new HashSet<>();

    // A map from blocked user ID to the detailed information
    private final Map<String, BlockingEntry> blockedAccounts = new HashMap<>();

    public GroupProfile(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    public Set<String> getAdminIds() {
        synchronized (adminIds) {
            return ImmutableSet.copyOf(adminIds);
        }
    }

    public void addAdminId(@Nonnull String adminId) {
        synchronized (adminIds) {
            adminIds.add(adminId);
        }
    }

    public void removeAdminId(@Nonnull String adminId) {
        synchronized (adminIds) {
            adminIds.remove(adminId);
        }
    }

    @Nullable
    public BlockingEntry getBlockingEntry(@Nonnull String userId) {
        synchronized (blockedAccounts) {
            BlockingEntry entry = blockedAccounts.get(userId);
            if (entry != null && entry.isExpired()) {
                blockedAccounts.remove(userId);
                return null;
            }
            return entry;
        }
    }

    @Nullable
    public BlockingEntry removeBlockingEntry(@Nonnull String userId) {
        synchronized (blockedAccounts) {
            return blockedAccounts.remove(userId);
        }
    }

    public void addBlockedUser(@Nonnull String userId) {
        synchronized (blockedAccounts) {
            blockedAccounts.put(userId, new BlockingEntry(userId));
            Iterator<Map.Entry<String, BlockingEntry>> iterator = blockedAccounts.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, BlockingEntry> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
                }
            }
        }
    }
}
