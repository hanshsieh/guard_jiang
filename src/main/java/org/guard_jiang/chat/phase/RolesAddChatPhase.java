package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.*;
import org.guard_jiang.chat.AccountSelectChatPhase;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 4/24/2017.
 */
public class RolesAddChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RolesAddChatPhase.class);

    /**
     * Required argument.
     */
    public static final String ARG_ROLE = "role";

    /**
     * Internal key.
     */
    private static final String KEY_LICENSE_ID = "licenseKey";

    /**
     * Internal key.
     */
    private static final String KEY_GROUP_ID = "groupId";

    private static final String KEY_MAX_NUM_ROLES = "max_num_roles";

    private static final String KEY_SEL_ACCOUNT_IDS = "selected_account_ids";

    public RolesAddChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        addRole();
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        switch (returnStatus) {
            case LICENSE_SELECT:
                onLicenseSelectFinish(returnData);
                break;
            case GROUP_SELECT:
                onGroupSelectFinish(returnData);
                break;
            case ACCOUNTS_SELECT:
                onAccountsSelectFinish(returnData);
                break;
            default:
                throw new IllegalStateException("Unexpected return from status: " + returnStatus);
        }
        if (isLeaving()) {
            return;
        }
        addRole();
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        // Do nothing
    }

    private void addRole() throws IOException {
        if (hasMissingInfo()) {
            return;
        }

        ObjectNode data = getData();
        ArrayNode selAccountIds = data.withArray(KEY_SEL_ACCOUNT_IDS);
        String groupId = data.get(KEY_GROUP_ID).asText();
        Guard guard = getGuard();
        GuardGroup group = guard.getGroup(groupId);
        Role role = getRole();
        long licenceId = data.get(KEY_LICENSE_ID).asLong();

        for (JsonNode selAccountIdNode : selAccountIds) {
            String selAccountId = selAccountIdNode.asText();
            group.addRole(selAccountId, role, licenceId);
        }
    }

    private boolean hasMissingInfo() throws IOException {
        ObjectNode data = getData();
        if (!data.has(KEY_LICENSE_ID)) {
            selectLicense();
            return true;
        }
        if (!data.has(KEY_GROUP_ID)) {
            selectGroup();
            return true;
        }
        if (!data.has(KEY_SEL_ACCOUNT_IDS)) {
            selectAccounts();
        }
        return false;
    }

    private void selectAccounts() throws IOException {
        ObjectNode data = getData();

        String groupId = data.get(KEY_GROUP_ID).asText();

        int availRoles = data.get(KEY_MAX_NUM_ROLES).asInt();

        Guard guard = getGuard();
        Set<String> guardIds = guard.getGuardIds();
        GuardGroup groupGuard = guard.getGroup(groupId);

        Set<String> usersWithRole = groupGuard.getRoles()
                .stream()
                .map(GroupRole::getUserId)
                .collect(Collectors.toSet());
        List<String> availableGuardIds = guardIds.stream()
                .filter(guardId -> !usersWithRole.contains(guardId))
                .collect(Collectors.toList());

        sendTextMessage("請選擇您想要用那些機器人來保護您的群組");
        ObjectNode arg = data.objectNode();
        ArrayNode accountIdsNode = data.arrayNode();

        arg.set(AccountSelectChatPhase.KEY_ACCOUNT_IDS, accountIdsNode);
        arg.put(AccountSelectChatPhase.KEY_MIN_NUM, 1);
        arg.put(AccountSelectChatPhase.KEY_MAX_NUM, availRoles);

        for (String guardId : availableGuardIds) {
            accountIdsNode.add(guardId);
        }
        startPhase(ChatStatus.ACCOUNTS_SELECT, arg);
    }

    private int getLicenseAvailableRoles() throws IOException {
        Guard guard = getGuard();
        ObjectNode data = getData();
        long licenseId = data.get(KEY_LICENSE_ID).asLong();
        License license;
        try {
            license = guard.getLicense(licenseId);
        } catch (IllegalArgumentException ex) {
            sendTextMessage("您選擇的金鑰可能已被移除囉！請重新選擇金鑰");
            data.remove(KEY_LICENSE_ID);
            selectLicense();
            return -1;
        }
        Role role = getRole();
        if (Role.DEFENDER.equals(role)) {
            return license.getMaxDefenders() - license.getNumDefenders();
        } else if (Role.SUPPORTER.equals(role)) {
            return license.getMaxSupporters() - license.getNumSupporters();
        } else {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }

    private void selectLicense() throws IOException {
        Role role = getRole();
        sendTextMessage("請選擇您想要為新增的"
                + role.name().toLowerCase()
                + "使用哪一個憑證");
        startPhase(ChatStatus.LICENSE_SELECT);
    }

    private void selectGroup() throws IOException {
        sendTextMessage("請選擇您要應用到的群組");
        startPhase(ChatStatus.GROUP_SELECT);
    }

    private void onAccountsSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        boolean canceled = returnData.get(AccountSelectChatPhase.RET_CANCELED).asBoolean();
        if (canceled) {
            leavePhase();
            return;
        }
        ObjectNode data = getData();
        ArrayNode selAccountIds = returnData.withArray(AccountSelectChatPhase.RET_SELECTED_ACCOUNT_IDS);
        data.set(KEY_SEL_ACCOUNT_IDS, selAccountIds.deepCopy());
    }

    private void onGroupSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (!returnData.has(GroupSelectChatPhase.RET_GROUP_ID)) {
            leavePhase();
            return;
        }
        String groupId = returnData.get(GroupSelectChatPhase.RET_GROUP_ID).asText();
        ObjectNode data = getData();
        data.set(KEY_GROUP_ID, data.textNode(groupId));
        sendTextMessage("您已選擇要應用到的群組");
    }

    private void onLicenseSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (!returnData.has(LicenseSelectChatPhase.RET_LICENSE)) {
            leavePhase();
            return;
        }
        String licenseKey = returnData.get(LicenseSelectChatPhase.RET_LICENSE).asText();

        Role role = getRole();
        int availRoles = getLicenseAvailableRoles();
        if(availRoles <= 0) {
            sendTextMessage(String.format("該金鑰已經沒有可選擇的%s了，請重新選擇", role));
            return;
        }

        ObjectNode data = getData();
        data.put(KEY_MAX_NUM_ROLES, availRoles);
        data.put(KEY_LICENSE_ID, licenseKey);
        sendTextMessage("您已成功選擇所要使用的金鑰");
    }

    @Nonnull
    private Role getRole() throws IOException {
        ObjectNode data = getData();
        int roleIdx = data.get(ARG_ROLE).asInt();
        try {
            return Role.values()[roleIdx];
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.error("Invalid role index {}", roleIdx);
            throw ex;
        }
    }
}
