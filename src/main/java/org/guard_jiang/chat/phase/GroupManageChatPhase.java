package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.Role;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This chat phase is used to manage the roles of a group.
 */
public class GroupManageChatPhase extends ChatPhase {

    protected static final String KEY_OPTIONS = "options";

    protected enum Option {
        MANAGE_DEFENDERS(1, "管理defenders"),
        MANAGE_SUPPORTERS(2, "管理supporters"),
        MANAGE_ADMINS(3, "管理admins"),
        DO_NOTHING(0, "返回");
        public final String text;
        public final int id;
        Option(int id, @Nonnull String text) {
            this.id = id;
            this.text = text;
        }

        public static Option fromId(int id) {
            for (Option option : Option.values()) {
                if (option.id == id) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Illegal option ID: " + id);
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
                new StringBuilder("此為群組管理介面，請輸入數字選擇您想要做的事情");
        int index = 0;
        for (Option item : Option.values()) {
            builder.append(String.format("\n%d: %s", ++index, item.text));
        }
        return builder.toString();
    }

    @Override
    public void onEnter() throws IOException {
        ObjectNode data = getData();
        ArrayNode optionsNode = data.arrayNode();
        for (Option option : Option.values()) {
            optionsNode.add(option.id);
        }
        data.set(KEY_OPTIONS, optionsNode);
        sendTextMessage(buildMessage());
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        leavePhase();
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        int optionIdx;
        try {
            optionIdx = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            onInvalidResponse();
            return;
        }
        JsonNode optionsNode = getData().get(KEY_OPTIONS);
        JsonNode optionIdNode = optionsNode.get(optionIdx - 1);
        if (optionIdNode == null) {
            onInvalidResponse();
            return;
        }
        Option option;
        try {
            option = Option.fromId(optionIdNode.asInt());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid option ID " + optionIdNode.asLong());
        }
        switch (option) {
            case MANAGE_DEFENDERS:
                onManageRole(Role.DEFENDER);
                break;
            case MANAGE_SUPPORTERS:
                onManageRole(Role.SUPPORTER);
                break;
            case MANAGE_ADMINS:
                onManageRole(Role.ADMIN);
                break;
            case DO_NOTHING:
                leavePhase();
                break;
            default:
                throw new IllegalArgumentException("Unexpected option: " + option);
        }
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }

    private void onManageRole(@Nonnull Role role) {
        ObjectNode arg = getData().objectNode();
        arg.put(RoleManageChatPhase.ARG_ROLE_ID, role.getId());
        startPhase(ChatStatus.ROLE_MANAGE, arg);
    }
}
