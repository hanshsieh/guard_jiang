package org.guard_jiang.message;

import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.ChatStatus;
import org.guard_jiang.Guard;
import org.guard_jiang.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public class MainMenuReplyMessageResponder extends MessageResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuReplyMessageResponder.class);

    private static final int MAX_TRIAL_LICENSE = 10;
    private static final int LICENSE_KEY_PREVIEW_LEN = 5;

    public MainMenuReplyMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata)
        throws IOException {
        String text = getMessageText(message);
        if (text == null) {
            onInvalidResponse();
            return;
        }
        int itemIdx;
        try {
            itemIdx = Integer.parseInt(text);
            MainMenuItem item = MainMenuItem.values()[itemIdx - 1];
            switch (item) {
                case CREATE_LICENSE:
                    onCreateLicense();
                    break;
                case PROTECT_GROUP:
                    onProtectGroup();
                    break;
                case DO_NOTHING:
                    onNothing();
                    break;
                default:
                    LOGGER.error("Menu item {} isn't handled", item);
                    onInvalidResponse();
                    break;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            onInvalidResponse();
        }
    }

    private void onCreateLicense() throws IOException {
        List<License> licenses = guard.getLicensesOfUser(account.getMid());
        if (licenses.size() >= MAX_TRIAL_LICENSE) {
            addTextResponse("您所建立的金鑰個數已經超過上限囉");
            setNewChatStatus(ChatStatus.USER_MAIN_MENU);
            setNewChatMetadata(Collections.emptyMap());
            return;
        }

        License license = guard.createTrialLicense();
        guard.bindLicenseToUser(license.getKey(), account.getMid());
        String key = guard.getLicenseKeyProvider().toReadableForm(license.getKey());
        addTextResponse("已為您建立金鑰並綁定至您的帳號\n金鑰: " + key);
        addTextResponse("您接下來可以用此金鑰保護您的群組");
        setNewChatStatus(ChatStatus.USER_MAIN_MENU_REPLY);
        setNewChatMetadata(Collections.emptyMap());
        addTextResponse(MainMenuMessageResponder.buildMessage());
    }

    private void onProtectGroup() throws IOException {
        List<License> licenses = guard.getLicensesOfUser(account.getMid());
        if (licenses.isEmpty()) {
            addTextResponse("您還沒有任何金鑰喔，請先建立一個");
            setNewChatStatus(ChatStatus.USER_MAIN_MENU_REPLY);
            setNewChatMetadata(Collections.emptyMap());
            addTextResponse(MainMenuMessageResponder.buildMessage());
            return;
        }
        addTextResponse("請選擇一個金鑰來使用: (輸入數字)");
        int idx = 0;
        Map<String, String> meta = new HashMap<>();
        for (License license : licenses) {
            String keyPrefix = license.getKey().substring(0, LICENSE_KEY_PREVIEW_LEN) + "...";
            addTextResponse(String.format("%d: \n金鑰: %s\n可用defender: %d\n可用supporter: %d",
                    idx + 1,
                    keyPrefix,
                    license.getMaxDefenders() - license.getNumDefenders(),
                    license.getMaxSupporters() - license.getNumSupporters()));
            meta.put(LicenseSelectMessageResponder.META_KEY_LICENSE_PREFIX + (idx + 1), license.getKey());
            ++idx;
        }
        meta.put(LicenseSelectMessageResponder.META_KEY_INCREASE, String.valueOf(true));
        setNewChatMetadata(meta);
        setNewChatStatus(ChatStatus.USER_SELECT_LICENSE_FOR_ADD);
    }

    private void onNothing() {
        setNewChatStatus(ChatStatus.USER_MAIN_MENU);
        setNewChatMetadata(Collections.emptyMap());
        addTextResponse("好的，有需要再敲我喔~");
    }

    private void onInvalidResponse() {
        addTextResponse(MainMenuMessageResponder.buildMessage());
    }
}
