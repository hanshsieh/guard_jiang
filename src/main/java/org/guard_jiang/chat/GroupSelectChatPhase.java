package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Group;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is a chatting phase for selecting a LINE group.
 * Currently, it only support selecting a group by entering a group invitation link.
 */
public class GroupSelectChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupSelectChatPhase.class);
    public static final String RET_GROUP_ID = "groupId";

    public GroupSelectChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        // TODO Allow selecting from a list of existing groups
        sendTextMessage(
                "請選擇一個群組，輸入該群組的邀請網址，例如：http://line.me/ti/g/abcdef\n" +
                "或輸入\"?\"取消此操作");
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        throw new IllegalStateException("Unexpected return status " + returnStatus);
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        text = text.trim();
        if ("?".equals(text)) {
            leavePhase();
        }
        URL url;
        try {
            url = new URL(text);
        } catch (MalformedURLException ex) {
            sendTextMessage("請輸入合法的邀請網址");
            return;
        }

        String ticketId;
        try {
            ticketId = getTicketIdFromGroupLink(url);
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("Fail to get ticket ID from URL", ex);
            sendTextMessage("請輸入合法的邀請網址");
            return;
        }
        Account account = getAccount();
        Group group;
        try {
            group = account.findGroupByTicket(ticketId);
        } catch (Exception ex) {
            LOGGER.debug("Fail to find group by ticket. ticketId: {}", ticketId, ex);
            sendTextMessage("找不到該群組");
            return;
        }
        ObjectNode data = getData();
        String groupId = group.getId();
        ObjectNode newData = data.objectNode();
        data.set(RET_GROUP_ID, data.textNode(groupId));
        leavePhase(newData);
    }

    /**
     * Get ticket ID from a group invitation link.
     * On iPhone, the group invitation link will look like: "http://line.me/ti/g/{ticketId}"
     * On Android, it will look like "http://line.me/R/ti/g/{ticketId}"
     *
     * @param url URL.
     * @return Ticket ID.
     *
     * @throws IllegalArgumentException The URL isn't a valid group invitation link.
     */
    private String getTicketIdFromGroupLink(URL url) throws IllegalArgumentException {
        String authority = url.getAuthority();
        if (!"line.me".equals(authority)) {
            throw new IllegalArgumentException("不合法的authority: " + authority);
        }
        String path = url.getPath();
        String[] pathTokens = path.split("/");
        return pathTokens[pathTokens.length - 1];
    }
}
