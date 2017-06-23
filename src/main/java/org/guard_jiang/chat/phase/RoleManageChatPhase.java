package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.Validate;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.Role;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This phase is used to manage a specific role.
 * User may use this phase to add or delete members of a role.
 */
public class RoleManageChatPhase extends ChatPhase {

    public static final String ARG_ROLE_ID = "role";
    protected static final String KEY_OPTIONS = "options";

    protected enum Option {
        ADD_ROLES(1, "增加%s"),
        REMOVE_ROLES(2, "移除%s"),
        GO_BACK(0, "返回");
        public final String text;
        public final int id;
        Option(int id, @Nonnull String text) {
            this.id = id;
            this.text = text;
        }

        public static Option fromId(long id) {
            for (Option option : Option.values()) {
                if (option.id == id) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Illegal option ID: " + id);
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
            String optionText = String.format(item.text, roleName);
            builder.append(String.format("\n%d: %s",
                    ++index,
                    optionText));
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
        Role role = getRole();
        sendTextMessage(buildMessage(role));
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
        } catch (IllegalArgumentException ex) {
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
                throw new IllegalArgumentException("Unsupported option " + option);
        }
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }

    private void onAddRoles() throws IOException {
        Role role = getRole();
        ObjectNode arg = getData().objectNode();
        arg.put(RolesAddChatPhase.ARG_ROLE_ID, role.getId());
        startPhase(ChatStatus.ROLES_ADD, arg);
    }

    private void onRemoveRoles() throws IOException {
        Role role = getRole();
        ObjectNode arg = getData().objectNode();
        arg.put(RolesRemoveChatPhase.ARG_ROLE_ID, role.getId());
        startPhase(ChatStatus.ROLES_REMOVE, arg);
    }

    @Nonnull
    private Role getRole() throws IOException {
        ObjectNode data = getData();
        JsonNode roleNode = data.get(ARG_ROLE_ID);
        Validate.notNull(
                roleNode,
                "No role is specified in the data. data: " + data);
        int roleId = roleNode.asInt();
        return Role.fromId(roleId);
    }
}
