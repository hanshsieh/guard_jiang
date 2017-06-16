package org.guard_jiang.chat.phase;

import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatFrame;

import javax.annotation.Nonnull;

/**
 * Factory for creating {@link ChatPhase}.
 */
public class ChatPhaseFactory {
    private final Guard guard;
    private final Account account;
    private final String userId;

    public ChatPhaseFactory(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String userId) {
        this.guard = guard;
        this.account = account;
        this.userId = userId;
    }

    public ChatPhase createChatPhase(@Nonnull ChatFrame chatFrame) {
        return chatFrame.getChatStatus().createChatPhase(
                guard,
                account,
                userId,
                chatFrame.getData()
        );
    }
}
