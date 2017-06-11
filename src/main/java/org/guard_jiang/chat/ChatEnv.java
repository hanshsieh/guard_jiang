package org.guard_jiang.chat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by someone on 4/15/2017.
 */
public class ChatEnv {
    private final ChatEnvType type;
    private final String id;
    public ChatEnv(@Nonnull ChatEnvType type, @Nonnull String id) {
        this.type = type;
        this.id = id;
    }

    @Nonnull
    public ChatEnvType getType() {
        return type;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "type: " + type
                + ", id: " + id;
    }
}
