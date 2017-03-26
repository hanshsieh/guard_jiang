package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 1/31/2017.
 */
public class Guard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guard.class);

    private final Map<String, AccountManager> accountMgrs = new HashMap<>();
    private boolean started = false;
    private final Storage storage;

    public Guard(@Nonnull Storage storage) throws IOException {
        this.storage = storage;
    }

    public synchronized void start() throws
            IOException,
            IllegalStateException,
            LoginFailureException {
        if (isStarted()) {
            throw new IllegalStateException("Already started");
        }
        started = true;
        release();
        try {
            reload();
        } catch (Exception ex) {
            release();
            throw ex;
        }
    }

    public synchronized void reload() throws IOException, LoginFailureException {
        if (!isStarted()) {
            throw new IllegalStateException("Not yet started");
        }
        Set<String> accountIds = storage.getAccountIds();
        Iterator<Map.Entry<String, AccountManager>> itr = accountMgrs.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, AccountManager> entry = itr.next();
            if (!accountIds.contains(entry.getKey())) {
                entry.getValue().stop();
                itr.remove();
            }
        }
        for (String accountId : storage.getAccountIds()) {
            if (accountMgrs.containsKey(accountId)) {
                continue;
            }
            AccountCredential credential = storage.getAccountCredential(accountId);
            if (credential == null) {
                LOGGER.warn("No account credential is available for {}", accountId);
                continue;
            }
            Account account = new Account(credential);
            try {
                account.login();
            } catch (Exception ex) {
                LOGGER.error("Fail to login to account {}. Skip it.", accountId, ex);
                continue;
            }
            accountMgrs.put(account.getMid(), new AccountManager(this, account));
            credential.setAuthToken(account.getAuthToken());
            credential.setCertificate(account.getCertificate());
            storage.setAccountCredential(accountId, credential);
        }

        for (AccountManager accountManager : accountMgrs.values()) {
            if (!accountManager.isStarted()) {
                accountManager.start();
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    public synchronized void stop() throws IllegalStateException {
        if (!isStarted()) {
            throw new IllegalStateException("Already stopped");
        }
        release();
        started = false;
    }

    private synchronized void release() {
        for (AccountManager accountManager : accountMgrs.values()) {
            if (accountManager.isStarted()) {
                accountManager.stop();
            }
        }
        accountMgrs.clear();
    }

    @Nonnull
    public GuardGroup getGroup(@Nonnull String groupId) throws IOException {
        return new GuardGroup(storage, groupId);
    }

    @Nonnull
    public Map<Relation, Role> getRoles() throws IOException {
        return storage.getRoles();
    }
}
