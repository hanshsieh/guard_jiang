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
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This phase is used to select a license of the user.
 */
public class LicenseSelectChatPhase extends ChatPhase {

    private static final int TIME_ZONE_OFFSET_HOURS = 8;
    private static final int LICENSE_PREFIX_LEN = 5;
    public static final String ARG_PROMPT = "prompt";
    protected static final String KEY_LICENSE_IDS = "licenses";

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

    private String prompt = null;

    public LicenseSelectChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        parseData();
        Guard guard = getGuard();
        List<License> licenses = guard.getLicensesOfUser(getUserId());
        if(licenses.isEmpty()) {
            sendTextMessage("您現在還沒有任何金鑰，請先建立一個");
            leavePhase(prepareReturnData(null));
            return;
        }
        sendTextMessage(prompt);
        sendTextMessage("請選擇一個金鑰(請輸入數字或\"?\"返回): ");
        ObjectNode data = getData();
        ArrayNode licensesNode = data.arrayNode();
        int index = 0;
        for (License license : licenses) {
            ++index;
            String licensePrefix = license.getKey().substring(0, LICENSE_PREFIX_LEN);
            String createTimeStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                    ZonedDateTime.ofInstant(
                            license.getCreateTime(), ZoneOffset.ofHours(TIME_ZONE_OFFSET_HOURS)));
            sendTextMessage(String.format(
                    "%d:\n" +
                    "  key: %s...\n" +
                    "  建立時間: %s\n" +
                    "  可用defender: %d\n" +
                    "  可用supporter: %d\n" +
                    "  可用admin: %d",
                    index,
                    licensePrefix,
                    createTimeStr,
                    license.getMaxDefenders() - license.getNumDefenders(),
                    license.getMaxSupporters() - license.getNumSupporters(),
                    license.getMaxAdmins() - license.getNumAdmins()));
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
        text = text.trim();
        if ("?".equals(text)) {
            leavePhase(prepareReturnData(null));
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
        leavePhase(prepareReturnData(licenseId));
    }

    private void parseData() {
        ObjectNode data = getData();
        prompt = data.get(ARG_PROMPT).asText();
    }

    @Nonnull
    private ObjectNode prepareReturnData(@Nullable String licenseId) {
        ObjectNode returnData = getData().objectNode();
        returnData.put(RET_CANCELED, licenseId == null);
        if (licenseId != null) {
            returnData.put(RET_LICENSE_ID, licenseId);
        }
        return returnData;
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔! (或輸入\"?\"返回)");
    }
}
