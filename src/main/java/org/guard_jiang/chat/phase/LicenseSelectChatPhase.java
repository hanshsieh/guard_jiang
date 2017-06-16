package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.License;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by someone on 4/24/2017.
 */
public class LicenseSelectChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseSelectChatPhase.class);
    private static final int LICENSE_PREFIX_LEN = 5;
    private static final String KEY_LICENSES = "licenses";
    public static final String RET_LICENSE = "license";

    public LicenseSelectChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        Guard guard = getGuard();
        Account account = getAccount();
        List<License> licenses = guard.getLicensesOfUser(account.getMid());
        if(licenses.isEmpty()) {
            sendTextMessage("您現在還沒有任何憑證，請先建立一個");
            leavePhase();
            return;
        }
        sendTextMessage("請選擇一個憑證(請輸入數字或\"?\"返回): ");
        ObjectNode data = getData();
        ArrayNode licensesNode = data.arrayNode();
        int index = 0;
        for (License license : licenses) {
            ++index;
            String licenseKey = license.getKey();
            String licensePrefix = licenseKey.substring(0, LICENSE_PREFIX_LEN);
            sendTextMessage(String.format(
                    "%d:\n" +
                    "  key: %s...\n" +
                    "  可用defender: %d\n" +
                    "  可用supporter: %d",
                    index,
                    licensePrefix,
                    license.getMaxDefenders() - license.getNumDefenders(),
                    license.getMaxSupporters() - license.getNumSupporters()));
            licensesNode.add(licenseKey);
        }
        data.set(KEY_LICENSES, licensesNode);
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        throw new IllegalStateException("Unexpected return status " + returnStatus);
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        if ("?".equals(text)) {
            leavePhase();
        }
        ObjectNode data = getData();
        JsonNode licenseNode = data.get(KEY_LICENSES);
        int licenseIdx;
        try {
            licenseIdx = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            onInvalidResponse();
            return;
        }

        if (licenseIdx < 0 || licenseIdx > licenseNode.size()) {
            onInvalidResponse();
            return;
        }

        String licenseKey = licenseNode.get(licenseIdx).asText();
        ObjectNode returnData = data.objectNode();
        returnData.set(RET_LICENSE, data.textNode(licenseKey));
        leavePhase(returnData);
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }
}
