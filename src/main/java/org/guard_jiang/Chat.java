package org.guard_jiang;

import org.guard_jiang.message.ChatEnv;
import org.guard_jiang.message.ChatEnvType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 4/8/2017.
 */
public class Chat {
    private final String hostId;
    private final String guestId;
    private final ChatEnv chatEnv;
    private ChatStatus status = ChatStatus.NONE;
    private Map<String, String> metadata = Collections.emptyMap();

    public Chat(
            @Nonnull String hostId,
            @Nonnull String guestId,
            @Nonnull ChatEnv chatEnv) {
        this.hostId = hostId;
        this.guestId = guestId;
        this.chatEnv = chatEnv;
    }

    @Nonnull
    public ChatEnv getChatEnv() {
        return chatEnv;
    }

    @Nonnull
    public String getHostId() {
        return hostId;
    }

    @Nonnull
    public String getGuestId() {
        return guestId;
    }

    @Nonnull
    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull ChatStatus status) {
        this.status = status;
    }

    @Nonnull
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nonnull Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
