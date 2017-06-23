package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Contact;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.phase.ChatPhase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by someone on 4/24/2017.
 */
public class AccountSelectChatPhase extends ChatPhase {

    public static final String ARG_MIN_NUM = "min_num";
    public static final String ARG_MAX_NUM = "max_num";
    public static final String ARG_ACCOUNT_IDS = "account_ids";
    public static final String RET_CANCELED = "canceled";
    public static final String RET_SELECTED_ACCOUNT_IDS = "selected_account_ids";

    private int minNum = -1;
    private int maxNum = -1;
    private String[] accountIds = null;

    public AccountSelectChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        Account account = getAccount();

        parseData();

        if(minNum > accountIds.length) {
            throw new IllegalStateException("Minimum selected accounts " + minNum
                    + " exceeds the number of accounts");
        }

        if (accountIds.length == 0) {
            leavePhase(prepareRetData(Collections.emptyList()));
            return;
        }

        sendTextMessage("請輸入逗點分隔的數字來選擇帳號，例如\"1,2,3\"，或\"?\"放棄此操作");
        sendTextMessage(String.format("您至少要選擇%d個，最多%d個", minNum, maxNum));
        int idx = 0;
        for (String accountId : accountIds) {
            Contact guardContact = account.getContact(accountId);
            sendTextMessage(String.format("%d:", idx + 1));
            account.sendContactMessage(guardContact.getMid());
        }
    }

    private ObjectNode prepareRetData(@Nullable List<String> selAccountIds) {
        ObjectNode data = getData();
        ObjectNode retData = data.objectNode();

        if (selAccountIds != null) {
            ArrayNode selAccountIdsNode = data.arrayNode();
            retData.set(RET_SELECTED_ACCOUNT_IDS, selAccountIdsNode);
            for (String selAccountId : selAccountIds) {
                selAccountIdsNode.add(selAccountId);
            }
        }
        retData.put(RET_CANCELED, selAccountIds != null);
        return retData;
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        throw new IllegalStateException("Unexpected return status " + returnStatus);
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        text = text.trim();
        if ("?".equals(text)) {
            leavePhase(prepareRetData(null));
            return;
        }
        parseData();
        String[] tokens = text.split("[,，]");
        List<String> selAccountIds = new ArrayList<>();
        for (String token : tokens) {
            int idx;
            try {
                idx = parseAccountIndex(token);
            } catch (IllegalArgumentException ex) {
                return;
            }
            String accountId = accountIds[idx];
            selAccountIds.add(accountId);
        }
        if (selAccountIds.size() < minNum || selAccountIds.size() > maxNum) {
            sendTextMessage(String.format("您輸入的數量不太對喔，請選擇至少%d個，最多%d個", minNum, maxNum));
            return;
        }
        leavePhase(prepareRetData(selAccountIds));
    }

    private int parseAccountIndex(@Nonnull String str) throws IOException {
        int idx;
        try {
            idx = Integer.parseInt(str.trim());
        } catch (NumberFormatException ex) {
            sendTextMessage("請輸入正確的數字喔!");
            throw new IllegalArgumentException("Illegal account index: " + str);
        }
        if (idx < 0 || idx > accountIds.length) {
            sendTextMessage("請輸入正確的數字喔!");
            throw new IllegalArgumentException("Account index out of range: " + idx);
        }
        return idx;
    }

    private void parseData() {
        ObjectNode data = getData();
        ArrayNode accountIdsNode = data.withArray(ARG_ACCOUNT_IDS);
        accountIds = new String[accountIdsNode.size()];
        for (int idx = 0; idx < accountIds.length; ++idx) {
            String accountId = accountIdsNode.get(idx).asText();
            accountIds[idx] = accountId;
        }
        minNum = data.get(ARG_MIN_NUM).asInt();
        minNum = data.get(ARG_MAX_NUM).asInt();
        if (minNum < 0 || maxNum < 0 || minNum > maxNum) {
            throw new IllegalArgumentException("Illegal minimum and maximum account number: min: "
                    + minNum + ", max: " + maxNum);
        }
    }
}
