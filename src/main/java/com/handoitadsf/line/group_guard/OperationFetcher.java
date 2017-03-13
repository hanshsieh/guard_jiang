package com.handoitadsf.line.group_guard;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cahsieh on 1/27/17.
 */
class OperationFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationFetcher.class);
    private static final int NUM_FETCH_OPERATIONS = 50;
    private final Account account;
    private OperationListener operationListener;
    private Fetcher fetcher;

    public OperationFetcher(@Nonnull Account account) {
        this.account = account;
    }
    public void setOperationListener(
            @Nullable OperationListener operationListener) {
        this.operationListener = operationListener;
    }

    public void start() throws IOException {
        if (fetcher != null) {
            throw new IllegalStateException("Already started");
        }
        long revision = account.getLastOpRevision();
        fetcher = new Fetcher(revision);
        fetcher.start();
    }

    public void stop() {
        if (fetcher == null) {
            throw new IllegalStateException("Already stopped");
        }
        fetcher.shouldStop();
        fetcher = null;
    }

    private class Fetcher extends Thread {

        private long SLEEP_MS = 1000 * 5;
        private boolean shouldStop = false;
        private long revision;

        public Fetcher(long revision) {
            this.revision = revision;
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
                while (!shouldStop) {
                    try {
                        LOGGER.debug("Send fetching operations request for account {} with revision {}",
                                account.getProfile().getMid(), revision);
                        List<Operation> operations = account.fetchOperations(
                                revision, NUM_FETCH_OPERATIONS);
                        for (Operation operation : operations) {
                            if (operationListener != null) {
                                operationListener.onOperation(operation);
                            }
                            revision = Math.max(revision, operation.getRevision());
                        }
                    } catch (Throwable ex) {
                        LOGGER.error("Error occurs when fetching operations. Retry later...", ex);
                        sleep();
                    }
                }
            } finally {
                 LOGGER.info("Fetcher stops");
            }
        }
    }

}
