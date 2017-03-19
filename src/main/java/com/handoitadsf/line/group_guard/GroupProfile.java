package com.handoitadsf.line.group_guard;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupProfile.class);

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

        LOGGER.info("Adding user {} to admin", adminId);

        synchronized (adminIds) {
            adminIds.add(adminId);
        }
    }

    public void addAdminIdIfEmpty(@Nonnull String adminId) {
        synchronized (adminIds) {
            if (adminIds.isEmpty()) {
                addAdminId(adminId);
            }
        }
    }

    public boolean removeAdminId(@Nonnull String adminId) {

        LOGGER.info("Removing user {} from admin", adminId);
        synchronized (adminIds) {
            return adminIds.remove(adminId);
        }
    }

    public boolean removeAdminIdIfNotEmpty(@Nonnull String adminId) {
        synchronized (adminIds) {
            if (adminIds.size() == 1 && adminIds.contains(adminId)) {
                return false;
            }
            return removeAdminId(adminId);
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
        LOGGER.info("Removing user {} from black list", userId);
        synchronized (blockedAccounts) {
            return blockedAccounts.remove(userId);
        }
    }

    public void addBlockedUser(@Nonnull String userId) {
        LOGGER.info("Adding user {} to black list", userId);
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
