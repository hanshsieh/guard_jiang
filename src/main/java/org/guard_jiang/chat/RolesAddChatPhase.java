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
public class RolesAddChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RolesAddChatPhase.class);

    public static final String KEY_ROLE = "role";
    public static final String KEY_LICENSE_KEY = "licenseKey";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_ACCOUNT_IDS = "accountIds";

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
        if (ChatStatus.LICENSE_SELECT.equals(returnStatus)) {
            onLicenseSelectFinish(returnData);
        } else if (ChatStatus.GROUP_SELECT.equals(returnStatus)) {
            onGroupSelectFinish(returnData);
        } else {
            throw new IllegalStateException("Unexpected return from status: " + returnStatus);
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
        // TODO
    }

    private boolean hasMissingInfo() throws IOException {
        ObjectNode data = getData();
        if (!data.has(KEY_LICENSE_KEY)) {
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
        sendTextMessage("請選擇您想要用那些機器人來保護您的群組");
        // TODO
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
        String license = returnData.get(LicenseSelectChatPhase.RET_LICENSE).asText();
        ObjectNode data = getData();
        data.set(KEY_LICENSE_KEY, data.textNode(license));
        sendTextMessage("您已成功選擇所要使用的金鑰");
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
