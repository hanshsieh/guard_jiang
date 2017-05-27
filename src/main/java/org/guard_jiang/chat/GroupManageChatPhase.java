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
public class GroupManageChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupManageChatPhase.class);

    private enum Option {
        MANAGE_DEFENDERS("管理defenders"),
        MANAGE_SUPPORTERS("管理supporters"),
        MANAGE_ADMINS("管理admins"),
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

    public GroupManageChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    private static String buildMessage() {
        StringBuilder builder =
                new StringBuilder("您好，請問有什麼需要幫忙的嗎? (請輸入數字)");
        int index = 0;
        for (Option item : Option.values()) {
            builder.append(String.format("\n%d: %s", ++index, item.getText()));
        }
        return builder.toString();
    }

    @Override
    public void onEnter() throws IOException {
        sendTextMessage(buildMessage());
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
            case MANAGE_DEFENDERS:
                onManageDefenders();
                break;
            case MANAGE_SUPPORTERS:
                onManageSupporters();
                break;
            case MANAGE_ADMINS:
                onManageAdmins();
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

    private void onManageDefenders() throws IOException {
        onManageRole(Role.DEFENDER);
    }

    private void onManageSupporters() throws IOException {
        onManageRole(Role.SUPPORTER);
    }

    private void onManageAdmins() throws IOException {
        onManageRole(Role.ADMIN);
    }

    private void onManageRole(@Nonnull Role role) {
        ObjectNode arg = getData().objectNode();
        arg.put(RoleManageChatPhase.KEY_ROLE, role.ordinal());
        startPhase(ChatStatus.ROLE_MANAGE, arg);
    }
}
