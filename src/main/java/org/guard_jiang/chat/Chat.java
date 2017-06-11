package org.guard_jiang.chat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by someone on 4/8/2017.
 */
public class Chat {
    private long id;
    private final String guardId;
    private final String userId;
    private final ChatEnv chatEnv;
    private final Deque<ChatFrame> stack;
    private Instant updateTime;

    public Chat(
            long id,
            @Nonnull String guardId,
            @Nonnull String userId,
            @Nonnull ChatEnv chatEnv,
            @Nonnull Deque<ChatFrame> stack,
            @Nonnull Instant updateTime) {
        this.id = id;
        this.guardId = guardId;
        this.userId = userId;
        this.chatEnv = chatEnv;
        this.stack = new ArrayDeque<>(stack);
        this.updateTime = updateTime;
    }

    public Chat(
            @Nonnull String guardId,
            @Nonnull String userId,
            @Nonnull ChatEnv chatEnv) {
        this(
                -1,
                guardId,
                userId,
                chatEnv,
                new ArrayDeque<>(),
                Instant.now()
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nonnull
    public ChatEnv getChatEnv() {
        return chatEnv;
    }

    @Nonnull
    public String getGuardId() {
        return guardId;
    }

    @Nonnull
    public Deque<ChatFrame> getStack() {
        return stack;
    }

    @Nonnull
    public String getUserId() {
        return userId;
    }

    @Nonnull
    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(@Nonnull Instant updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "id: " + id
                + ", guardId: " + guardId
                + ", userId: " + userId
                + ", chatEnv: [" + chatEnv + "]"
                + ", updateTs: " + updateTime
                + ", stack: " + stack;
    }
}
