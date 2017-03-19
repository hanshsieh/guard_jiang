package com.handoitadsf.line.group_guard;

import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;

/**
 * Created by someone on 3/18/2017.
 */
public class AccountWatcher extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountWatcher.class);
    private static final int MAX_RETRY = 3;
    private static final int NUM_FETCH_OPERATIONS = 50;
    private static final long AUTH_TOKEN_REFRESH_MS = 1000 * 60 * 60 * 12;
    private static final long SLEEP_MS = 1000 * 3;
    private static final long RECTIFY_STATUS_DELAY_MS = 1000 * 60 * 10;

    private boolean shouldStop = false;
    private Long revision = null;
    private final AccountManager accountManager;

    public AccountWatcher(@Nonnull AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void shouldStop() {
        this.shouldStop = true;
        interrupt();
    }

    private void sleep() {
        try {
            LOGGER.debug("Sleeping for {} ms", SLEEP_MS);
            Thread.sleep(SLEEP_MS);
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted from sleeping", ex);
        }
    }

    @Override
    public void run() {
        try {
            int retryCount = 0;
            while (!shouldStop) {
                try {
                    Account account = accountManager.getAccount();

                    long now = System.currentTimeMillis();
                    long authTokenAgeMs = now - account.getAuthTokenLastRefreshTime().toEpochMilli();
                    if (authTokenAgeMs > AUTH_TOKEN_REFRESH_MS) {
                        account.refreshAuthToken();
                    }

                    if (revision == null) {
                        revision = account.getLastOpRevision();
                        accountManager.rectifyStatus();
                    }
                    Instant lastRectifyTime = accountManager.getLastRectifyTime();

                    if (lastRectifyTime == null ||
                            now - lastRectifyTime.toEpochMilli() > RECTIFY_STATUS_DELAY_MS) {
                        accountManager.rectifyStatus();
                    }

                    List<Operation> operations = account.fetchOperations(
                            revision, NUM_FETCH_OPERATIONS);
                    for (Operation operation : operations) {
                        accountManager.onOperation(operation);
                        revision = Math.max(revision, operation.getRevision());
                        retryCount = 0;
                    }
                } catch (Throwable ex) {
                    ++retryCount;
                    if (retryCount >= MAX_RETRY) {
                        LOGGER.error("Error occurs when handling operations. Skip it. retryCount: {}",
                                retryCount, ex);
                        revision++;
                    } else {
                        LOGGER.error("Error occurs when handling operations. Retry later. retryCount: {}",
                                retryCount, ex);
                    }
                    sleep();
                }
            }
        } finally {
            LOGGER.info("AccountWatcher stops");
        }
    }
}
