package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.phase.*;

import javax.annotation.Nonnull;

/**
 * It represents a chatting status.
 * Each {@link ChatStatus} has a corresponding {@link ChatPhase}.
 */
public enum ChatStatus {
    // Common
    // 1 ~ 1000
    LICENSE_CREATE(1) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new LicenseCreationChatPhase(
                    guard, account, userId, data);
        }
    },
    GROUP_MANAGE(2) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new GroupManageChatPhase(
                    guard, account, userId, data);
        }
    },
    ROLE_MANAGE(3) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new RoleManageChatPhase(
                    guard, account, userId, data
            );
        }
    },
    ROLES_ADD(4) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new RolesAddChatPhase(
                    guard, account, userId, data);
        }
    },
    ROLES_REMOVE(5) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new RolesRemoveChatPhase(
                    guard, account, userId, data);
        }
    },
    LICENSE_SELECT(6) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new LicenseSelectChatPhase(
                    guard, account, userId, data);
        }
    },
    GROUP_SELECT(7) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new GroupSelectChatPhase(
                    guard, account, userId, data);
        }
    },
    ACCOUNTS_SELECT(8) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new AccountsSelectChatPhase(
                    guard, account, userId, data);
        }
    },
    ACCOUNTS_INPUT(9) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new AccountsInputChatPhase(
                    guard, account, userId, data);
        }
    },

    // Chat with a user
    // 1001 ~ 2000
    USER_MAIN_MENU(1001) {
        @Nonnull
        @Override
        public ChatPhase createChatPhase(
                @Nonnull Guard guard,
                @Nonnull Account account,
                @Nonnull String userId,
                @Nonnull ObjectNode data) {
            return new UserMainMenuChatPhase(
                    guard, account, userId, data);
        }
    };

    // Chat in group
    // 3001 ~ 4000

    // Chat in room
    // 4001 ~ 5000

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

    @Nonnull
    public abstract ChatPhase createChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String userId,
            @Nonnull ObjectNode data
    );
}
