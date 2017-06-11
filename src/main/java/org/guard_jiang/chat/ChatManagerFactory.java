package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;

import javax.annotation.Nonnull;

/**
 * A factory for chat manager.
 */
public class ChatManagerFactory {
    private final Account account;
    private final Guard guard;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public ChatManagerFactory(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
    }

    @Nonnull
    public ChatManager createChatManager(@Nonnull ChatEnv chatEnv, @Nonnull String userId) {
        return new ChatManager(guard, account, chatEnv, userId, objectMapper);
    }
}
