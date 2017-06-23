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
    private static final String KEY_LICENSE_IDS = "licenses";

    /**
     * A key of the return data, whose value should be a boolean denoting whether
     * this phase is canceled.
     * If the phase is canceled, the return data won't contain other fields.
     */
    public static final String RET_CANCELED = "canceled";

    /**
     * A key of the return data, whose value should be a string if the license ID.
     */
    public static final String RET_LICENSE_ID = "licenseId";

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
            onCanceled();
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
            licensesNode.add(license.getId());
        }
        data.set(KEY_LICENSE_IDS, licensesNode);
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        throw new IllegalStateException("Unexpected return status " + returnStatus);
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        if ("?".equals(text)) {
            onCanceled();
            return;
        }
        ObjectNode data = getData();
        JsonNode licenseIdsNode = data.get(KEY_LICENSE_IDS);
        int licenseIdx;
        try {
            licenseIdx = Integer.parseInt(text) - 1;
        } catch (NumberFormatException ex) {
            onInvalidResponse();
            return;
        }

        JsonNode licenseIdNode = licenseIdsNode.get(licenseIdx);
        if (licenseIdNode == null) {
            onInvalidResponse();
            return;
        }

        String licenseId = licenseIdNode.asText();
        ObjectNode returnData = data.objectNode();
        returnData.put(RET_LICENSE_ID, licenseId);
        returnData.put(RET_CANCELED, false);
        leavePhase(returnData);
    }

    private void onCanceled() {
        ObjectNode ret = getData().objectNode();
        ret.put(RET_CANCELED, true);
        leavePhase(ret);
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }
}
