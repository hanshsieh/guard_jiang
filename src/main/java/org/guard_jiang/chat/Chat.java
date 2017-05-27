package org.guard_jiang.chat;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by someone on 4/8/2017.
 */
public class Chat {
    private final String hostId;
    private final String guestId;
    private final ChatEnv chatEnv;
    private final Deque<ChatFrame> stack;

    public Chat(
            @Nonnull String hostId,
            @Nonnull String guestId,
            @Nonnull ChatEnv chatEnv,
            @Nonnull Deque<ChatFrame> stack) {
        this.hostId = hostId;
        this.guestId = guestId;
        this.chatEnv = chatEnv;
        this.stack = new ArrayDeque<>(stack);
    }

    public Chat(
            @Nonnull String hostId,
            @Nonnull String guestId,
            @Nonnull ChatEnv chatEnv) {
        this(hostId, guestId, chatEnv, new ArrayDeque<>());
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
    public Deque<ChatFrame> getStack() {
        return stack;
    }

    @Nonnull
    public String getGuestId() {
        return guestId;
    }
}
