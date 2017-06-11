package org.guard_jiang;

import com.typesafe.config.Config;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;
import org.guard_jiang.storage.MyBatisSqlSessionFactory;
import org.guard_jiang.storage.SqlStorage;
import org.guard_jiang.storage.Storage;
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

    public Guard(@Nonnull Config config) throws IOException {
        MyBatisSqlSessionFactory sessionFactory = new MyBatisSqlSessionFactory(
                config.getConfig("sqlReader"),
                config.getConfig("sqlWriter")
        );
        this.storage = new SqlStorage(config.getConfig("storage"), sessionFactory);
        this.licenseKeyProvider = new LicenseKeyProvider();
    }

    public Guard(
            @Nonnull Storage storage,
            @Nonnull LicenseKeyProvider licenseKeyProvider) throws IOException {
        this.storage = storage;
        this.licenseKeyProvider = licenseKeyProvider;
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
        List<AccountData> accountsData = storage.getGuardAccounts(true);
        Set<String> accountIds = accountsData
                .stream()
                .map(AccountData::getMid)
                .collect(Collectors.toSet());
        Iterator<Map.Entry<String, AccountManager>> itr = accountMgrs.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, AccountManager> entry = itr.next();
            if (!accountIds.contains(entry.getKey())) {
                entry.getValue().stop();
                itr.remove();
            }
        }
        for (AccountData accountData : accountsData) {
            String mid = accountData.getMid();
            if (accountMgrs.containsKey(mid)) {
                continue;
            }
            Credential credential = accountData.getCredential();
            assert credential != null;
            Account account = new Account(credential);
            try {
                account.login();
            } catch (Exception ex) {
                LOGGER.error("Fail to login to account {}. Skip it.", mid, ex);
                continue;
            }
            accountMgrs.put(account.getMid(), new AccountManager(this, account));
            credential.setAuthToken(account.getAuthToken());
            credential.setCertificate(account.getCertificate());
            storage.updateGuardAccount(accountData);
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

    public Set<String> getGuardIds() throws IOException {
        return storage.getGuardAccounts(false)
                .stream()
                .map(AccountData::getMid)
                .collect(Collectors.toSet());
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
    public License getLicense(long licenseId) throws IOException {
        return storage.getLicense(licenseId);
    }

    @Nonnull
    public License createTrialLicense(@Nonnull String userId) throws IOException {
        String key = licenseKeyProvider.buildLicenseKey();
        License license = new License(
                key,
                userId,
                Instant.now());
        license.setMaxDefenders(TRIAL_MAX_DEFENDERS);
        license.setMaxSupporters(TRIAL_MAX_SUPPORTERS);
        storage.createLicense(license);
        return license;
    }

    @Nonnull
    public LicenseKeyProvider getLicenseKeyProvider() {
        return licenseKeyProvider;
    }
}
