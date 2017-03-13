package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cahsieh on 1/27/17.
 */
class GroupRefresher extends PrioritizedTask {

    private final GuardRole role;

    public GroupRefresher(GuardRole role, int priority) {
        super(priority);
        this.role = role;
    }

    @Override
    public void run() throws Exception {
        role.refreshGroups();
    }
}
