package org.guard_jiang;

import com.typesafe.config.Config;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.exception.LoginFailureException;
import org.guard_jiang.services.license.LicenseKeyService;
import org.guard_jiang.services.storage.sql.SqlSessionFactory;
import org.guard_jiang.services.storage.sql.SqlStorage;
import org.guard_jiang.services.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The core of the Guard Jiang service.
 */
public class Guard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guard.class);
    private static final int TRIAL_MAX_DEFENDERS = 2;
    private static final int TRIAL_MAX_SUPPORTERS = 1;
    private static final int TRIAL_MAX_ADMINS = 5;

    private final Map<String, AccountManager> accountMgrs = new HashMap<>();
    private boolean started = false;
    private final Storage storage;
    private final LicenseKeyService licenseKeyService;

    public Guard(@Nonnull Config config) throws IOException {
        SqlSessionFactory sessionFactory = new SqlSessionFactory(
                config.getConfig("sqlReader"),
                config.getConfig("sqlWriter")
        );
        this.storage = new SqlStorage(config.getConfig("storage"), sessionFactory);
        this.licenseKeyService = new LicenseKeyService();
    }

    public Guard(
            @Nonnull Storage storage,
            @Nonnull LicenseKeyService licenseKeyService) throws IOException {
        this.storage = storage;
        this.licenseKeyService = licenseKeyService;
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
        List<Account> newAccounts = storage.getGuardAccounts().get();
        Set<String> newAccountIds = newAccounts
                .stream()
                .map(Account::getMid)
                .collect(Collectors.toSet());
        Iterator<Map.Entry<String, AccountManager>> itr = accountMgrs.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, AccountManager> entry = itr.next();
            if (!newAccountIds.contains(entry.getKey())) {
                entry.getValue().stop();
                itr.remove();
            }
        }
        for (Account newAccount : newAccounts) {
            String mid = newAccount.getMid();
            if (accountMgrs.containsKey(mid)) {
                continue;
            }
            try {
                newAccount.login();
            } catch (Exception ex) {
                LOGGER.error("Fail to login to account {}. Skip it.", mid, ex);
                continue;
            }
            accountMgrs.put(newAccount.getMid(), new AccountManager(this, newAccount));
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
    public Chat getChat(@Nonnull String guardId, @Nonnull String userId, @Nonnull ChatEnv env)
            throws IOException {
        Chat chat = storage.getChat(guardId, userId, env);
        if (chat == null) {
            chat = new Chat(guardId, userId, env);
        }
        return chat;
    }

    public void setChat(@Nonnull Chat chat) throws IOException {
        storage.setChat(chat);
    }

    public Set<String> getGuardIds() throws IOException {
        return storage.getGuardAccounts().get()
                .stream()
                .map(Account::getMid)
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Group getGroup(@Nonnull String groupId) throws IOException {
        return new Group(storage, groupId);
    }

    /**
     * Get the set of ID's of groups which a given user have created a role inside.
     *
     * @param userId User's LINE mid.
     * @return Set of group ID's.
     */
    @Nonnull
    public Set<String> getGroupsWithRolesCreatedByUser(@Nonnull String userId) {
        return storage.getGroupsWithRolesCreatedByUser(userId);
    }

    @Nonnull
    public List<License> getLicensesOfUser(@Nonnull String userId) throws IOException {
        return storage.getLicensesOfUser(userId);
    }

    @Nonnull
    public License getLicense(@Nonnull String licenseId) throws IOException {
        return storage.getLicense(licenseId);
    }

    @Nonnull
    public License createTrialLicense(@Nonnull String userId) throws IOException {
        String key = licenseKeyService.genLicenseKey();
        License license = new License(
                null,
                key,
                userId,
                Instant.now());
        license.setMaxDefenders(TRIAL_MAX_DEFENDERS);
        license.setMaxSupporters(TRIAL_MAX_SUPPORTERS);
        license.setMaxAdmins(TRIAL_MAX_ADMINS);
        storage.createLicense(license);
        return license;
    }

    @Nonnull
    public LicenseKeyService getLicenseKeyService() {
        return licenseKeyService;
    }
}
