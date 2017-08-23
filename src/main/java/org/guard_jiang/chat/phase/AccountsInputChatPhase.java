package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Contact;
import org.apache.commons.lang3.Validate;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This phase is to let user specify a list LINE accounts.
 */
public class AccountsInputChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsInputChatPhase.class);

    public static final String ARG_MIN_NUM = "minNum";
    public static final String ARG_MAX_NUM = "maxNum";

    // Optional argument
    public static final String ARG_SELECTED_ACCOUNT_IDS = "selectedAccountIds";
    public static final String RET_CANCELED = "canceled";
    public static final String RET_SELECTED_ACCOUNT_IDS = "selectedAccountIds";

    private int minNum = -1;
    private int maxNum = -1;
    private Set<String> selectedAccountIds;

    public AccountsInputChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        parseData();
        if (selectedAccountIds.size() >= maxNum) {
            leavePhase(prepareRetData(selectedAccountIds));
            return;
        }
        if (minNum > 0) {
            sendTextMessage(String.format("您可選擇至少%d個，最多%d個LINE帳號", minNum, maxNum));
        } else {
            sendTextMessage(String.format("您可選擇最多%d個LINE帳號", maxNum));
        }
        sendInstruction();
    }

    private void sendInstruction() throws IOException {
        if (selectedAccountIds.size() >= minNum) {
            sendTextMessage("若您已選擇完畢，請輸入\"done\"");
        }
        sendTextMessage("請用以下其中一種方式指定:\n"
                + "1. 傳送LINE聯絡人資訊(按\"＋\" -> 聯絡資訊 -> 選擇LINE好友)\n"
                + "2. 輸入\"me\"選擇您自己\n"
                + "或者您可輸入\"?\"放棄此操作");
    }

    private ObjectNode prepareRetData(@Nullable Set<String> selAccountIds) {
        ObjectNode data = getData();
        ObjectNode retData = data.objectNode();

        if (selAccountIds != null) {
            ArrayNode selAccountIdsNode = data.arrayNode();
            retData.set(RET_SELECTED_ACCOUNT_IDS, selAccountIdsNode);
            for (String selAccountId : selAccountIds) {
                selAccountIdsNode.add(selAccountId);
            }
        }
        retData.put(RET_CANCELED, selAccountIds == null);
        return retData;
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        throw new IllegalStateException("Unexpected return status " + returnStatus);
    }

    @Override
    protected void onReceiveContactMessage(@Nonnull String contactId) throws IOException {
        parseData();
        if (addSelectedAccount(contactId)) {
            onFinishSelection();
            return;
        }
        saveData();
        sendInstruction();
    }


    @Override
    protected void onReceiveTextMessage(@Nonnull String text) throws IOException {
        text = text.trim().toLowerCase(Locale.US);
        if ("?".equals(text) || "？".equals(text)) {
            leavePhase(prepareRetData(null));
            return;
        }
        parseData();
        if ("done".equals(text)) {
            onFinishSelection();
            return;
        }
        if ("me".equals(text)) {
            if (addSelectedAccount(getUserId())) {
                onFinishSelection();
                return;
            }
            saveData();
        } else {
            sendTextMessage("輸入錯誤");
        }
        sendInstruction();
    }

    private void onFinishSelection() throws IOException {
        if (selectedAccountIds.size() < minNum) {
            sendTextMessage("您尚未選擇足夠的帳號");
            sendInstruction();
            return;
        }
        Validate.isTrue(selectedAccountIds.size() <= maxNum,
                "Exceeding maximum number of accounts");
        sendTextMessage("已完成帳號選擇");
        leavePhase(prepareRetData(selectedAccountIds));
    }

    private void parseData() {
        ObjectNode data = getData();
        selectedAccountIds = new HashSet<>();
        if (data.has(ARG_SELECTED_ACCOUNT_IDS)) {
            ArrayNode accountIdsNode = data.withArray(ARG_SELECTED_ACCOUNT_IDS);
            for (JsonNode accountIdNode : accountIdsNode) {
                selectedAccountIds.add(accountIdNode.asText());
            }
        }
        if (data.has(ARG_MIN_NUM)) {
            minNum = data.get(ARG_MIN_NUM).asInt();
        } else {
            minNum = 0;
        }
        maxNum = data.get(ARG_MAX_NUM).asInt();
        if (minNum < 0 || maxNum < 0 || minNum > maxNum) {
            throw new IllegalArgumentException("Illegal minimum and maximum account number: min: "
                    + minNum + ", max: " + maxNum);
        }

        if(selectedAccountIds.size() > maxNum) {
            throw new IllegalStateException("Selected accounts exceed maximum number. numSelected: "
                    + selectedAccountIds.size() + ", max: " + maxNum);
        }
    }

    private boolean addSelectedAccount(@Nonnull String selAccountId) throws IOException {
        selAccountId = selAccountId.trim().toLowerCase(Locale.US);
        if (selectedAccountIds.contains(selAccountId)) {
            sendTextMessage("這個帳號已經選過了");
            return false;
        }
        Account account = getAccount();
        Contact selContact = account.findAndAddContactById(selAccountId);
        if (selContact == null) {
            LOGGER.warn("Invalid LINE account ID is entered. accountId: {}", selAccountId);
            sendTextMessage("找不到該帳號");
            return false;
        }
        Validate.isTrue(selectedAccountIds.size() < maxNum,
                "Exceeding maximum number of accounts");
        selectedAccountIds.add(selAccountId);
        sendTextMessage(String.format("總共已選擇%d個帳號", selectedAccountIds.size()));
        return selectedAccountIds.size() == maxNum;
    }

    private void saveData() {
        ObjectNode data = getData();
        ArrayNode accountIdsNode = data.arrayNode();
        for (String accountId : selectedAccountIds) {
            accountIdsNode.add(accountId);
        }
        data.set(ARG_SELECTED_ACCOUNT_IDS, accountIdsNode);
    }
}
