package com.handoitadsf.line.group_guard;

import com.google.common.collect.ImmutableSet;
import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
public class Guard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guard.class);

    private final Map<String, AccountManager> accountMgrs;
    private final List<OperationFetcher> opFetchers = new ArrayList<>();

    // A map from group ID to group profile
    private final Map<String, GroupProfile> groupProfiles;

    // A map from group ID to the defenders in the group
    private final Map<String, Set<String>> groupDefenders;
    private final Map<Relation, Role> roles;
    private boolean started = false;

    Guard(
            @Nonnull Collection<Account> accounts,
            @Nonnull Collection<GroupProfile> groups,
            @Nonnull Map<Relation, Role> roles) throws IOException {
        this.accountMgrs = accounts
                .stream()
                .collect(Collectors.toMap(
                        account -> {
                            try {
                                return account.getProfile().getMid();
                            } catch (IOException ex) {
                                throw new RuntimeException("Fail to get profile", ex);
                            }
                        },
                        account -> new AccountManager(this, account)
                        ));
        this.groupProfiles = groups.stream()
                .collect(Collectors.toMap(GroupProfile::getGroupId, groupProfile -> groupProfile));

        this.roles = new HashMap<>(roles);

        // For each account
        for (AccountManager accountManager : this.accountMgrs.values()) {
            Account account = accountManager.getAccount();

            // Setup a OperationFetcher for the account
            OperationFetcher opFetcher = new OperationFetcher(account);
            opFetcher.setOperationListener(accountManager);
            opFetchers.add(opFetcher);
        }

        this.groupDefenders = roles.entrySet()
                .stream()
                .filter(entry -> Role.DEFENDER.equals(entry.getValue())) // Filter out the defenders
                .map(Map.Entry::getKey) // Get a stream of relations for defender role
                .collect(Collectors.groupingBy(Relation::getGroupId, // Collect the result by group ID
                        Collectors.mapping(Relation::getUserId, Collectors.toSet())));
    }

    public void start() throws IOException, IllegalStateException {
        if (started) {
            throw new IllegalStateException("Already started");
        }
        started = true;
        for (OperationFetcher opFetcher : opFetchers) {
            try {
                opFetcher.start();
            } catch (IllegalStateException ex) {
                LOGGER.error("An OperationFetcher seems to have been started before", ex);
            }
        }
    }

    public void stop() throws IllegalStateException {
        if (!started) {
            throw new IllegalStateException("Already stopped");
        }
        for (OperationFetcher opFetcher : opFetchers) {
            try {
                opFetcher.stop();
            } catch (IllegalStateException ex) {
                LOGGER.error("An OperationFetcher seems to have been stopped before", ex);
            }
        }
        started = false;
    }

    @Nullable
    public Role getRole(@Nonnull String userId, @Nonnull String groupId) {
        return roles.get(new Relation(userId, groupId));
    }

    @Nonnull
    public Set<String> getAccountIds() {
        return Collections.unmodifiableSet(accountMgrs.keySet());
    }

    @Nonnull
    public Set<String> getGroupRoleMembers(@Nonnull String groupId, @Nonnull Role role) {
        return roles.entrySet()
                .stream()
                .filter(entry ->
                        // Filter out the users with the role and group
                        groupId.equals(entry.getKey().getGroupId()) &&
                                role.equals(entry.getValue()))
                .map(entry -> entry.getKey().getUserId())
                .collect(Collectors.toSet());
    }

    @Nullable
    public GroupProfile getGroupProfile(@Nonnull String groupId) {
        return groupProfiles.get(groupId);
    }
}
