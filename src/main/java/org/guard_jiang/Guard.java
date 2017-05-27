package org.guard_jiang;

import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 1/31/2017.
 */
public class Guard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guard.class);
    private static final int TRIAL_MAX_DEFENDERS = 2;
    private static final int TRIAL_MAX_SUPPORTERS = 1;

    private final Map<String, AccountManager> accountMgrs = new HashMap<>();
    private boolean started = false;
    private final Storage storage;
    private final LicenseKeyProvider licenseKeyProvider;

    public Guard(@Nonnull Storage storage) throws IOException {
        this.storage = storage;
        this.licenseKeyProvider = new LicenseKeyProvider();
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
        Set<String> accountIds = storage.getUserIds();
        Iterator<Map.Entry<String, AccountManager>> itr = accountMgrs.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, AccountManager> entry = itr.next();
            if (!accountIds.contains(entry.getKey())) {
                entry.getValue().stop();
                itr.remove();
            }
        }
        for (String accountId : accountIds) {
            if (accountMgrs.containsKey(accountId)) {
                continue;
            }
            Credential credential = storage.getCredential(accountId);
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
            storage.setCredential(accountId, credential);
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
    public Chat getChat(@Nonnull String hostId, @Nonnull String guestId, @Nonnull ChatEnv env)
            throws IOException {
        Chat chat = storage.getChat(hostId, guestId, env);
        if (chat == null) {
            chat = new Chat(hostId, guestId, env);
        }
        return chat;
    }

    public void setChat(@Nonnull Chat chat) throws IOException {
        storage.setChat(chat);
    }

    @Nonnull
    public GuardGroup getGroup(@Nonnull String groupId) throws IOException {
        return new GuardGroup(storage, groupId);
    }

    @Nonnull
    public List<License> getLicensesOfUser(@Nonnull String userId) throws IOException {
        return storage.getLicensesOfUser(userId);
    }

    @Nonnull
    public License createTrialLicense() throws IOException {
        String key = licenseKeyProvider.buildLicenseKey();
        License license = new License(key);
        license.setCreateTime(Instant.now());
        license.setMaxDefenders(TRIAL_MAX_DEFENDERS);
        license.setMaxSupporters(TRIAL_MAX_SUPPORTERS);
        storage.createLicense(license);
        return license;
    }

    public void bindLicenseToUser(@Nonnull String licenseKey, @Nonnull String userId) throws IOException {
        storage.bindLicenseToUser(licenseKey, userId);
    }

    @Nonnull
    public LicenseKeyProvider getLicenseKeyProvider() {
        return licenseKeyProvider;
    }
}
