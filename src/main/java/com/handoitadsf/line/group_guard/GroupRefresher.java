package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cahsieh on 1/27/17.
 */
class GroupRefresher extends PrioritizedTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupRefresher.class);

    private final GuardRole role;

    public GroupRefresher(GuardRole role, int priority) {
        super(priority);
        this.role = role;
    }

    public void run() {
        try {
            role.getLineClient().refreshGroups();
        } catch (Exception ex) {
            LOGGER.error("Fail to refresh groups: ", ex);
        } finally {
            role.addTask(new GroupRefresher(role, getPriority()));
        }
    }
}
