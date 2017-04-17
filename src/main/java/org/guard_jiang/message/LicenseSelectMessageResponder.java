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
public class LicenseSelectMessageResponder extends MessageResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseSelectMessageResponder.class);
    public static final String META_KEY_LICENSE_PREFIX = "license_";
    public static final String META_KEY_INCREASE = "increase";

    public LicenseSelectMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata)
        throws IOException {
        String text = getMessageText(message);
        LOGGER.debug("Text: {}, metadata: {}", text, metadata);

        if (text == null) {
            onInvalidResponse();
            return;
        }

        Map<String, String> newMeta = new HashMap<>();
        int itemIdx;
        try {
            itemIdx = Integer.parseInt(text);
            String licenseKey = metadata.get(META_KEY_LICENSE_PREFIX + itemIdx);
            if (licenseKey == null) {
                onInvalidResponse();
                return;
            }
            newMeta.put(GroupSelectForLicenseMessageResponder.META_KEY_LICENSE, licenseKey);
            newMeta.put(GroupSelectForLicenseMessageResponder.META_KEY_INCREASE, metadata.get(META_KEY_INCREASE));
            setNewChatMetadata(newMeta);
            setNewChatStatus(ChatStatus.USER_SELECT_GROUP_FOR_LICENSE_ADD);
            addTextResponse("請輸入您想要變更的群組的邀請網址\n例如: http://line.me/R/ti/g/abcd");
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            onInvalidResponse();
        }
    }

    private void onInvalidResponse() {
        addTextResponse("請輸入正確的金鑰編號");
    }
}
