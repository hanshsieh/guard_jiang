package org.guard_jiang.chat;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/8/2017.
 */
public enum ChatStatus {
    // Common
    // 1 ~ 1000
    LICENSE_CREATE(1),
    GROUP_MANAGE(2),
    ROLE_MANAGE(3),
    ROLES_ADD(4),
    ROLES_REMOVE(5),
    LICENSE_SELECT(6),
    GROUP_SELECT(7),

    // Chat with a user
    // 1001 ~ 2000
    USER_MAIN_MENU(1001);

    // Chat in group
    // 3001 ~ 4000

    // Chat in room
    // 4001 ~ 5000

    private final int id;
    ChatStatus(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    @Nonnull
    public static ChatStatus fromId(int id) {
        for (ChatStatus status : ChatStatus.values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ChatStatus ID " + id);
    }
}
