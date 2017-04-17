package org.guard_jiang.message;

import line.thrift.Group;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.ChatStatus;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by someone on 4/9/2017.
 */
public class GroupSelectForLicenseMessageResponder extends MessageResponder {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupSelectForLicenseMessageResponder.class);
    private static final Pattern TICKET_ID_PAT = Pattern.compile("/ti/g/([^/]+)");
    public static final String META_KEY_LICENSE = "license";
    public static final String META_KEY_INCREASE = "increase";
    public static final String META_KEY_GROUP_ID = "group_id";

    public GroupSelectForLicenseMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata)
        throws IOException {
        String urlStr = getMessageText(message);
        if (urlStr == null) {
            onInvalidResponse();
            return;
        }
        urlStr = urlStr.trim();
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            onInvalidResponse();
            return;
        }
        String urlPath = url.getPath();
        Matcher matcher = TICKET_ID_PAT.matcher(urlPath);
        if (!matcher.find()) {
            onInvalidResponse();
            return;
        }
        String ticketId = matcher.group(1);
        Group group = account.findGroupByTicket(ticketId);
        addTextResponse("群組名稱: " + group.getName());
        Map<String, String> newMeta = new HashMap<>(metadata);
        newMeta.put(META_KEY_GROUP_ID, group.getId());
        setNewChatMetadata(newMeta);
        setNewChatStatus(ChatStatus.);
    }

    private void onInvalidResponse() {
        addTextResponse("請輸入群組的邀請網址");
    }
}
