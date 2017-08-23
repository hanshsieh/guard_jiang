package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.Role;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

/**
 * This phase is for removing a role in a group.
 * Given a role type, it allows users to select a group, and then delete a role in the group.
 */
public class RolesRemoveChatPhase extends ChatPhase {

    /**
     * Required argument.
     */
    public static final String ARG_ROLE_ID = "role";

    /**
     * Optional argument.
     */
    public static final String ARG_GROUP_ID = "group_id";

    private String groupId = null;
    private Role role = null;

    public RolesRemoveChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        parseData();
        if (groupId == null) {
            Guard guard = getGuard();
            Set<String> groupIds = guard.getGroupsWithRolesCreatedByUser(getUserId());
            for (String groupId : groupIds) {

            }
        }
        sendTextMessage("抱歉，此功能目前尚未支援喔");
        leavePhase();
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        leavePhase();
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        leavePhase();
    }

    private void parseData() {
        groupId = null;
        ObjectNode data = getData();
        if (data.has(ARG_GROUP_ID)) {
            groupId = data.get(ARG_GROUP_ID).asText();
        }
        role = Role.fromId(data.get(ARG_ROLE_ID).asInt());
    }

    private void saveData() {
        ObjectNode data = getData();
        if (groupId != null) {
            data.put(ARG_GROUP_ID, groupId);
        }
    }
}
