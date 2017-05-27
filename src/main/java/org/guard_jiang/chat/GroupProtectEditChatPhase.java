package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.License;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by someone on 4/22/2017.
 */
public class GroupProtectEditChatPhase extends ChatPhase {

    private static final int LICENSE_KEY_PREVIEW_LEN = 5;

    public GroupProtectEditChatPhase(
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
        if (licenses.isEmpty()) {
            sendTextMessage("您還沒有任何金鑰喔，請先建立一個");
            leavePhase();
            return;
        }
        sendTextMessage("請選擇一個金鑰來使用: (輸入數字)");
        int idx = 0;
        Map<String, String> meta = new HashMap<>();
        for (License license : licenses) {
            String keyPrefix = license.getKey().substring(0, LICENSE_KEY_PREVIEW_LEN) + "...";
            addTextResponse(String.format("%d: \n金鑰: %s\n可用defender: %d\n可用supporter: %d",
                    idx + 1,
                    keyPrefix,
                    license.getMaxDefenders() - license.getNumDefenders(),
                    license.getMaxSupporters() - license.getNumSupporters()));
            meta.put(LicenseSelectChatter.META_KEY_LICENSE_PREFIX + (idx + 1), license.getKey());
            ++idx;
        }
        meta.put(LicenseSelectChatter.META_KEY_INCREASE, String.valueOf(true));
        setNewChatMetadata(meta);
        setNewChatStatus(ChatStatus.USER_SELECT_LICENSE_FOR_ADD);
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {

    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {

    }
}
