package org.guard_jiang;

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
