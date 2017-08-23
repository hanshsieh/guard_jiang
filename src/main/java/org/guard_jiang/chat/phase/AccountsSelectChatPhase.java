package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Contact;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;
import org.guard_jiang.chat.phase.ChatPhase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * This phase is for selecting from a list of guard accounts.
 * The guard accounts being selected should have been added to contacts of the guard account
 * talking to the user.
 */
public class AccountsSelectChatPhase extends ChatPhase {

    public static final String ARG_MIN_NUM = "minNum";
    public static final String ARG_MAX_NUM = "maxNum";
    public static final String ARG_ACCOUNT_IDS = "accountIds";
    public static final String RET_CANCELED = "canceled";
    public static final String RET_SELECTED_ACCOUNT_IDS = "selectedAccountIds";

    private int minNum = -1;
    private int maxNum = -1;
    private String[] accountIds = null;

    public AccountsSelectChatPhase(
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

        if (accountIds.length == 0) {
            leavePhase(prepareRetData(Collections.emptySet()));
            return;
        }

        sendTextMessage("請輸入逗點分隔的數字來選擇帳號，例如\"1,2,3\"，或\"?\"放棄此操作");
        sendTextMessage(String.format("您至少要選擇%d個，最多%d個", minNum, maxNum));
        int idx = 0;
        for (String accountId : accountIds) {
            sendTextMessage(String.format("%d:", idx + 1));
            account.sendContactMessage(getUserId(), accountId);
        }
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
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        text = text.trim();
        if ("?".equals(text)) {
            leavePhase(prepareRetData(null));
            return;
        }
        parseData();
        String[] tokens = text.split("[,，]");
        Set<String> selAccountIds = new HashSet<>();
        for (String token : tokens) {
            int idx;
            try {
                idx = parseAccountIndex(token);
            } catch (IllegalArgumentException ex) {
                sendTextMessage(String.format("您所輸入的\"%s\"不是合法的的數字喔!(或可輸入\"?\"取消此操作)", token));
                return;
            }
            String accountId = accountIds[idx];
            selAccountIds.add(accountId);
        }
        if (selAccountIds.size() < minNum || selAccountIds.size() > maxNum) {
            sendTextMessage(String.format("您輸入的數量不對喔，請選擇至少%d個，最多%d個", minNum, maxNum));
            return;
        }
        leavePhase(prepareRetData(selAccountIds));
    }

    private int parseAccountIndex(@Nonnull String str) throws IOException {
        int idx;
        try {
            idx = Integer.parseInt(str.trim()) - 1;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Illegal account index: " + str);
        }
        if (idx < 0 || idx >= accountIds.length) {
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
        if (data.has(ARG_MIN_NUM)) {
            minNum = data.get(ARG_MIN_NUM).asInt();
        } else {
            minNum = 0;
        }
        if (data.has(ARG_MAX_NUM)) {
            maxNum = data.get(ARG_MAX_NUM).asInt();
        } else {
            maxNum = accountIds.length;
        }
        if (minNum < 0 || maxNum < 0 || minNum > maxNum) {
            throw new IllegalArgumentException("Illegal minimum and maximum account number: min: "
                    + minNum + ", max: " + maxNum);
        }

        if(minNum > accountIds.length) {
            throw new IllegalArgumentException("Minimum selected accounts " + minNum
                    + " exceeds the number of accounts");
        }
    }
}
