package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 1/31/2017.
 */
public class GuardBuilder {

    /**
     * A map from mid to account.
     */
    private final Map<String, Account> accounts = new HashMap<>();

    // Set of group ID's
    private final Map<String, GroupProfile> groups = new HashMap<>();
    private final Map<Relation, Role> userRoles = new HashMap<>();

    public GuardBuilder addAccount(@Nonnull Account account) throws IOException {
        String id = account.getProfile().getMid();
        if (accounts.containsKey(id)) {
            throw new IllegalStateException("Account " + id + " already exists");
        }
        accounts.put(id, account);
        return this;
    }

    public GuardBuilder addGroup(@Nonnull GroupProfile groupProfile) {
        String groupId = groupProfile.getGroupId();
        if (groups.containsKey(groupId)) {
            throw new IllegalStateException("Group " + groupId + " already exists");
        }
        groups.put(groupId, groupProfile);
        return this;
    }

    public GuardBuilder addRole(@Nonnull String userId, @Nonnull String groupId, @Nonnull Role role) {
        if (!groups.containsKey(groupId)) {
            throw new IllegalStateException("Group " + groupId + " not added");
        }
        Relation relation = new Relation(userId, groupId);
        Role oldRole = userRoles.get(relation);
        if (oldRole != null) {
            throw new IllegalStateException("User " + userId + " already has " + role + " in group " + groupId);
        }
        userRoles.put(relation, role);
        return this;
    }

    public Guard build() throws IOException {
        return new Guard(accounts, groups, userRoles);
    }
}
