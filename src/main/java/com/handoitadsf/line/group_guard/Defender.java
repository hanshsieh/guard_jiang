package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Defender implements GuardRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(Defender.class);

    private static final int SCHEDULED_THREAD_POOL_CORE_SIZE = 1;
    private static final long STATE_UPDATE_DELAY_MS = 1000 * 30;

    private static final int PRIORITY_STATE_FETCH = 1;
    private static final int PRIORITY_OPERATION_FETCH = 5;

    private boolean running = false;
    private final LineClient client;
    private PrioritizedExecutor executor;
    private GroupsUpdateListener groupsUpdateListener;

    public Defender(Account account) {
        this.client = account.getClient();
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("It is already running");
        }
        init();
    }

    public void setGroupsUpdateListener(GroupsUpdateListener listener) {
        this.groupsUpdateListener = listener;
    }

    private void init() {
        executor = new PrioritizedExecutor();
        OperationFetcher operationFetcher = new OperationFetcher(this, PRIORITY_OPERATION_FETCH);
        operationFetcher.setListener(new PrioritizedTask.Listener() {
            @Override
            public void onStop() {
                executor.submit(operationFetcher);
            }

            @Override
            public void onStop(@Nonnull Throwable throwable) {
                LOGGER.error("Fail to fetch operations. Retry later. ", throwable);
                onStop();
            }
        });
        executor.submit(operationFetcher);

        GroupRefresher groupRefresher = new GroupRefresher(this, PRIORITY_STATE_FETCH);
        groupRefresher.setListener(new PrioritizedTask.Listener() {
            @Override
            public void onStop(@Nonnull Throwable error) {
                LOGGER.error("Fail to refresh groups. Retry later. ", error);
                executor.submit(groupRefresher);
            }

            @Override
            public void onStop() {
                if (groupsUpdateListener != null) {
                    groupsUpdateListener.onGroupsUpdate(Defender.this, client.getGroups());
                }
            }
        });
        executor.submit(groupRefresher);
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("It is already stopped");
        }
        executor.shutdown();
        executor = null;
    }

    @Override
    public List<LineGroup> getGroups() {
        return client.getGroups();
    }
}
