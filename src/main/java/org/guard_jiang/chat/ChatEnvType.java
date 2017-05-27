package org.guard_jiang.chat;

/**
 * Created by someone on 4/13/2017.
 */
public enum ChatEnvType {
    USER(0),
    ROOM(1),
    GROUP(2);
    private final int id;
    ChatEnvType(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public static ChatEnvType fromId(int id) {
        for (ChatEnvType type : ChatEnvType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid environment type ID " + id);
    }
}
