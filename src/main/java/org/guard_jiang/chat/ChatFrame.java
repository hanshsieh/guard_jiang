package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/22/2017.
 */
public class ChatFrame {
    private final ChatStatus chatStatus;
    private ObjectNode data;

    public ChatFrame(@Nonnull ChatStatus chatStatus, @Nonnull ObjectNode data) {
        this.chatStatus = chatStatus;
        this.data = data;
    }

    @Nonnull
    public ChatStatus getChatStatus() {
        return chatStatus;
    }

    @Nonnull
    public ObjectNode getData() {
        return data;
    }

    public void setData(@Nonnull ObjectNode data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "status: " + chatStatus + ", data: " + data;
    }
}
