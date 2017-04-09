package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/8/2017.
 */
public enum ChatStatus {
    NONE(0),
    MAIN_MENU(1),
    MAIN_MENU_REPLY(2),
    REGISTER_GROUP(3),
    REGISTER_GROUP_REPLY(4);
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
