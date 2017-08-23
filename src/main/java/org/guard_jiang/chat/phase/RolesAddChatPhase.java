package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Contact;
import org.apache.commons.lang3.Validate;
import org.guard_jiang.*;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This phase is for adding some guard accounts as a specific role to a group.
 */
public class RolesAddChatPhase extends ChatPhase {

    /**
     * Required arguments.
     */
    public static final String ARG_ROLE_ID = "role";

    /**
     * Internal keys.
     */
    protected static final String KEY_LICENSE_ID = "licenseId";
    protected static final String KEY_GROUP_ID = "groupId";
    protected static final String KEY_ACCOUNT_IDS = "accountIds";

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
            case ACCOUNTS_INPUT:
                onAccountsInputFinish(returnData);
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
        throw new IllegalStateException("Receive unexpected message: " + text);
    }

    private void addRole() throws IOException {
        if (hasMissingInfo()) {
            return;
        }

        ObjectNode data = getData();
        ArrayNode selAccountIds = data.withArray(KEY_ACCOUNT_IDS);
        String groupId = data.get(KEY_GROUP_ID).asText();
        Guard guard = getGuard();
        Group group = guard.getGroup(groupId);
        Role role = getRole();
        String licenceId = data.get(KEY_LICENSE_ID).asText();

        Group groupGuard = guard.getGroup(groupId);

        Map<String, GroupRole> existingRoles = groupGuard.getRoles()
                .stream()
                .collect(Collectors.toMap(GroupRole::getUserId, r -> r));
        Account account = getAccount();
        int numAdded = 0;
        for (JsonNode selAccountIdNode : selAccountIds) {
            String selAccountId = selAccountIdNode.asText().trim().toLowerCase();
            Contact selContact = account.findAndAddContactById(selAccountId);
            if (selContact == null) {
                sendTextMessage("咦~有一個帳號找不到，略過該唱號");
                continue;
            }
            GroupRole existingRole = existingRoles.get(selAccountId);
            if (existingRole != null) {
                String name = selContact.getDisplayNameOverridden();
                if (name == null) {
                    name = selContact.getDisplayName();
                }
                sendTextMessage(String.format("帳號\"%s\"已經在該群組擔任%s了，略過此帳號",
                        name,
                        existingRole.getRole().name().toLowerCase()));
                continue;
            }
            group.addRole(selAccountId, role, licenceId);
            ++numAdded;
        }
        sendTextMessage(String.format("已成功加入%d個%s", numAdded, role.name().toLowerCase()));
        leavePhase();
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
        if (!data.has(KEY_ACCOUNT_IDS)) {
            selectAccounts();
            return true;
        }
        return false;
    }

    private void selectAccounts() throws IOException {

        ObjectNode data = getData();

        String licenseId = data.get(KEY_LICENSE_ID).asText();
        int availRoles;

        try {
            availRoles = getLicenseAvailableRoles(licenseId);
        } catch (IllegalArgumentException ex) {
            sendTextMessage("您選擇的金鑰可能已被移除囉！請重新選擇金鑰");
            data.remove(KEY_LICENSE_ID);
            selectLicense();
            return;
        }

        sendTextMessage("請選擇要將哪些帳號指定為" + getRole().name().toLowerCase());

        Role role = getRole();
        if (Role.ADMIN.equals(role)) {
            selectUserAccounts(availRoles);
        } else {
            selectGuardAccounts(availRoles);
        }
    }

    private void selectUserAccounts(int availRoles) throws IOException {
        ObjectNode arg = getData().objectNode();
        arg.put(AccountsInputChatPhase.ARG_MIN_NUM, 1);
        arg.put(AccountsInputChatPhase.ARG_MAX_NUM, availRoles);
        startPhase(ChatStatus.ACCOUNTS_INPUT, arg);
    }

    private void selectGuardAccounts(int availRoles) throws IOException {
        ObjectNode data = getData();

        String groupId = data.get(KEY_GROUP_ID).asText();

        Guard guard = getGuard();
        Set<String> guardIds = guard.getGuardIds();
        Group groupGuard = guard.getGroup(groupId);

        Set<String> usersWithRole = groupGuard.getRoles()
                .stream()
                .map(GroupRole::getUserId)
                .collect(Collectors.toSet());
        List<String> availableGuardIds = guardIds.stream()
                .filter(guardId -> !usersWithRole.contains(guardId))
                .collect(Collectors.toList());

        if (availableGuardIds.isEmpty()) {
            sendTextMessage("不好意思，已經沒有帳號可供選擇了");
            leavePhase();
            return;
        }

        ObjectNode arg = data.objectNode();
        ArrayNode accountIdsNode = data.arrayNode();

        arg.set(AccountsSelectChatPhase.ARG_ACCOUNT_IDS, accountIdsNode);
        arg.put(AccountsSelectChatPhase.ARG_MIN_NUM, 1);

        if (availRoles > 0) {
            arg.put(AccountsSelectChatPhase.ARG_MAX_NUM, availRoles);
        }

        for (String guardId : availableGuardIds) {
            accountIdsNode.add(guardId);
        }
        startPhase(ChatStatus.ACCOUNTS_SELECT, arg);
    }

    private int getLicenseAvailableRoles(@Nonnull String licenseId) throws IOException {
        Guard guard = getGuard();
        License license = guard.getLicense(licenseId);
        Role role = getRole();
        if (Role.DEFENDER.equals(role)) {
            return license.getMaxDefenders() - license.getNumDefenders();
        } else if (Role.SUPPORTER.equals(role)) {
            return license.getMaxSupporters() - license.getNumSupporters();
        } else {
            return license.getMaxAdmins() - license.getNumAdmins();
        }
    }

    private void selectLicense() throws IOException {
        Role role = getRole();
        ObjectNode arg = getData().objectNode();
        arg.put(LicenseSelectChatPhase.ARG_PROMPT, "請選擇您想要為新增的"
                + role.name().toLowerCase()
                + "使用哪一個金鑰");
        startPhase(ChatStatus.LICENSE_SELECT, arg);
    }

    private void selectGroup() throws IOException {
        sendTextMessage("請選擇您要應用到的群組");
        startPhase(ChatStatus.GROUP_SELECT);
    }

    private void onAccountsSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (returnData.get(AccountsSelectChatPhase.RET_CANCELED).asBoolean()) {
            leavePhase();
            return;
        }
        ObjectNode data = getData();
        ArrayNode selAccountIds = returnData.withArray(AccountsSelectChatPhase.RET_SELECTED_ACCOUNT_IDS);
        data.set(KEY_ACCOUNT_IDS, selAccountIds.deepCopy());
    }

    private void onAccountsInputFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (returnData.get(AccountsInputChatPhase.RET_CANCELED).asBoolean()) {
            leavePhase();
            return;
        }
        ObjectNode data = getData();
        ArrayNode selAccountIds = returnData.withArray(AccountsInputChatPhase.RET_SELECTED_ACCOUNT_IDS);
        data.set(KEY_ACCOUNT_IDS, selAccountIds.deepCopy());
    }

    private void onGroupSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (returnData.get(GroupSelectChatPhase.RET_CANCELED).asBoolean()) {
            leavePhase();
            return;
        }
        String groupId = returnData.get(GroupSelectChatPhase.RET_GROUP_ID).asText();
        ObjectNode data = getData();
        data.set(KEY_GROUP_ID, data.textNode(groupId));
        sendTextMessage("您已選擇要應用到的群組");
    }

    private void onLicenseSelectFinish(@Nonnull ObjectNode returnData) throws IOException {
        if (returnData.get(LicenseSelectChatPhase.RET_CANCELED).asBoolean()) {
            leavePhase();
            return;
        }
        String licenseId = returnData.get(LicenseSelectChatPhase.RET_LICENSE_ID).asText();

        ObjectNode data = getData();
        data.put(KEY_LICENSE_ID, licenseId);
        sendTextMessage("您已成功選擇所要使用的金鑰");
    }

    @Nonnull
    private Role getRole() {
        ObjectNode data = getData();
        JsonNode roleNode = data.get(ARG_ROLE_ID);
        Validate.notNull(
                roleNode,
                "No role is specified in the data. data: " + data);
        int roleId = roleNode.asInt();
        return Role.fromId(roleId);
    }
}
