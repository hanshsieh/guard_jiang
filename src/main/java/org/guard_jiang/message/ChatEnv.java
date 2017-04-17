package org.guard_jiang.message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by someone on 4/15/2017.
 */
public class ChatEnv {
    private final ChatEnvType type;
    private final String id;
    public ChatEnv(@Nonnull ChatEnvType type, @Nullable String id) {
        this.type = type;
        this.id = id;
    }

    public ChatEnvType getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
