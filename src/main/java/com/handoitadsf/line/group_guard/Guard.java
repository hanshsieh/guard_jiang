package com.handoitadsf.line.group_guard;

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
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
public class Guard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guard.class);
    private static final int THREAD_CORE_POOL_SIZE = 0;
    private static final int THREAD_MAX_POOL_SIZE = 100;
    private static final int THREAD_QUEUE_SIZE = 1000;
    private static final long THREAD_KEEP_ALIVE_MS = 1000 * 30;

    private final Map<String, AccountManager> accountMgrs;
    private final List<OperationFetcher> opFetchers = new ArrayList<>();

    // A map from group ID to group profile
    private final Map<String, GroupProfile> groupProfiles;

    // A map from group ID to the defenders in the group
    private final Map<String, Set<String>> groupDefenders;
    private final Map<Relation, Role> roles;
    private ExecutorService executor;
    private boolean started = false;

    Guard(
            @Nonnull Collection<Account> accounts,
            @Nonnull Map<String, GroupProfile> groups,
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
        this.groupProfiles = new HashMap<>(groups);
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
        if (executor != null) {
            executor.shutdown();
        }
        executor = new ThreadPoolExecutor(
                THREAD_CORE_POOL_SIZE,
                THREAD_MAX_POOL_SIZE,
                THREAD_KEEP_ALIVE_MS,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(THREAD_QUEUE_SIZE));
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
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        started = false;
    }

    @Nullable
    public Role getRole(@Nonnull String userId, @Nonnull String groupId) {
        if (!accountMgrs.containsKey(userId) || !groupProfiles.containsKey(groupId)) {
            return null;
        }
        Role role = roles.get(new Relation(userId, groupId));
        if (role == null) {
            return Role.SUPPORTER;
        } else {
            return role;
        }
    }

    @Nonnull
    public Set<String> getAccountIds() {
        return Collections.unmodifiableSet(accountMgrs.keySet());
    }

    @Nonnull
    public Set<String> getSupportersOfGroup(@Nonnull String groupId) {
        return getAccountIds().stream().filter(id ->
            !roles.containsKey(new Relation(id, groupId))
        ).collect(Collectors.toSet());
    }

    @Nonnull
    public Set<String> getDefendersOfGroup(@Nonnull String groupId) {
        Set<String> defenderIds = groupDefenders.get(groupId);
        if (defenderIds == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(defenderIds);
        }
    }

    @Nonnull
    public GroupProfile getGroupProfile(@Nonnull String groupId) {
        GroupProfile groupProfile = groupProfiles.get(groupId);
        if (groupProfile == null) {
            throw new IllegalArgumentException("Group ID " + groupId + " isn't managed by this instance");
        }
        return groupProfile;
    }

    public void submitOperation(@Nonnull String userId, @Nonnull Operation operation) {
        if (!started) {
            throw new IllegalStateException("Not yet started");
        }
        AccountManager accountManager = accountMgrs.get(userId);
        if (accountManager == null) {
            throw new IllegalArgumentException("User " + userId + " isn't managed by this instance");
        }
        executor.submit(new OperationDispatcher(accountManager, operation));
    }
}
