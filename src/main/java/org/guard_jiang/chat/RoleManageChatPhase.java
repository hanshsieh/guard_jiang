package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by someone on 4/24/2017.
 */
public class RoleManageChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleManageChatPhase.class);

    public static final String KEY_ROLE = "role";

    private enum Option {
        ADD_ROLES("增加%s"),
        REMOVE_ROLES("移除%s"),
        GO_BACK("返回");
        private final String text;
        Option(@Nonnull String text) {
            this.text = text;
        }

        @Nonnull
        public String getText() {
            return text;
        }
    }

    public RoleManageChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    private static String buildMessage(@Nonnull Role role) {
        String roleName = role.name().toLowerCase();
        StringBuilder builder =
                new StringBuilder(
                        String.format("這是%s管理介面，您想要我幫您什麼? (請輸入數字)", roleName));
        int index = 0;
        for (Option item : Option.values()) {
            String optionText = String.format(item.getText(), roleName);
            builder.append(String.format("\n%d: %s",
                    ++index,
                    optionText));
        }
        return builder.toString();
    }

    @Override
    public void onEnter() throws IOException {
        Role role = getRole();
        sendTextMessage(buildMessage(role));
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        leavePhase();
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        Option item;
        try {
            int itemIdx = Integer.parseInt(text);
            item = Option.values()[itemIdx - 1];

        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            onInvalidResponse();
            return;
        }
        switch (item) {
            case ADD_ROLES:
                onAddRoles();
                break;
            case REMOVE_ROLES:
                onRemoveRoles();
                break;
            case GO_BACK:
                leavePhase();
                break;
            default:
                throw new IllegalArgumentException("Unsupported option " + item);
        }
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }

    private void onAddRoles() throws IOException {
        Role role = getRole();
        ObjectNode arg = getData().objectNode();
        arg.put(RolesAddChatPhase.KEY_ROLE, role.ordinal());
        startPhase(ChatStatus.ROLES_ADD, arg);
    }

    private void onRemoveRoles() throws IOException {
        Role role = getRole();
        ObjectNode arg = getData().objectNode();
        arg.put(RolesRemoveChatPhase.KEY_ROLE, role.ordinal());
        startPhase(ChatStatus.ROLES_REMOVE, arg);
    }

    @Nonnull
    private Role getRole() throws IOException {
        ObjectNode data = getData();
        int roleIdx = data.get(KEY_ROLE).asInt();
        try {
            return Role.values()[roleIdx];
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.error("Invalid role index {}", roleIdx);
            throw ex;
        }
    }
}
