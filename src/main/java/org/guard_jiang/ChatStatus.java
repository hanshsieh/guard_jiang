package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/8/2017.
 */
public enum ChatStatus {
    // 0 ~ 1000
    NONE(0),

    // Chat with a user
    // 1001 ~ 2000
    USER_MAIN_MENU(1001),
    USER_MAIN_MENU_REPLY(1002),
    USER_SELECT_LICENSE_FOR_ADD(1003),
    USER_SELECT_GROUP_FOR_LICENSE_ADD(1004);

    // Common for chatting with multi user (room or group)
    // 2001 ~ 3000

    // Chat in room
    // 3001 ~ 4000

    // Chat in group
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
