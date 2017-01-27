package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.OpType;
import line.thrift.Operation;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Defender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Defender.class);

    private static final int SCHEDULED_THREAD_POOL_CORE_SIZE = 1;
    private static final long STATE_UPDATE_DELAY_MS = 1000 * 30;
    private static final int NUM_FETCH_OPERATIONS = 100;

    private boolean running = false;
    private final LineClient client;
    private final List<LineGroup> groups = new ArrayList<LineGroup>();
    private ScheduledExecutorService scheduledExecutor;
    public Defender(Account account) {
        this.client = account.getClient();
    }
    public void start() {
        if (running) {
            throw new IllegalStateException("It is already running");
        }
        init();

    }

    private void init() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(SCHEDULED_THREAD_POOL_CORE_SIZE);
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("It is already stopped");
        }
        scheduledExecutor.shutdown();
        scheduledExecutor = null;
    }

    private class StateFetcher implements Runnable {

        public void run() {
            try {
                client.refreshGroups();
            } catch (Exception ex) {
                LOGGER.error("Fail to refresh groups: ", ex);
            }
        }
    }

    private class OperationFetcher implements Runnable {

        public void run() {
            try {
                List<Operation> operations = client.getApi().fetchOperations(
                    client.getRevision(), NUM_FETCH_OPERATIONS);
                for (Operation operation : operations) {

                    OpType opType = operation.getType();
                    switch (opType) {
                        case NOTIFIED_INVITE_INTO_GROUP:
                            // The current user has been invited to join a group.

                        case ACCEPT_GROUP_INVITATION:
                            // The current user has accepted a group invitation
                        case LEAVE_GROUP:
                            // The current user has left a group.
                    }

                    client.setRevision(Math.max(client.getRevision(), operation.getRevision()));
                }
            } catch (Exception ex) {

            }
        }
    }
}
