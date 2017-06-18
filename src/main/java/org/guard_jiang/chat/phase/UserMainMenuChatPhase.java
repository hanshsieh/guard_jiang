package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This chat phase is used as the main menu when chatting with a LINE user in a 1-1 chatting.
 */
public class UserMainMenuChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMainMenuChatPhase.class);
    protected static final String KEY_OPTIONS = "options";

    protected enum Option {
        CREATE_LICENSE("建立新的金鑰", 1),
        MANAGE_GROUP("管理群組保護狀態", 2),
        DO_NOTHING("目前沒有需要幫忙的", 3);
        public final String text;
        public final long id;
        Option(@Nonnull String text, long id) {
            this.text = text;
            this.id = id;
        }

        public static Option fromId(long id) {
            for (Option option : Option.values()) {
                if (option.id == id) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Illegal option ID: " + id);
        }
    }

    public UserMainMenuChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String userId,
            @Nonnull ObjectNode data) {
        super(guard, account, userId, data);
    }

    private static String buildMessage() {
        StringBuilder builder =
                new StringBuilder("您好，請問有什麼需要幫忙的嗎? (請輸入數字)");
        int index = 0;
        for (Option option : Option.values()) {
            builder.append(String.format("\n%d: %s", ++index, option.text));
        }
        return builder.toString();
    }

    @Override
    public void onEnter() throws IOException {
        ObjectNode data = getData();
        ArrayNode optionsNode = data.arrayNode();
        for (Option option : Option.values()) {
            optionsNode.add(option.id);
        }
        data.set(KEY_OPTIONS, optionsNode);
        sendTextMessage(buildMessage());
    }

    @Override
    public void onReturn(
            @Nonnull ChatStatus returnStatus,
            @Nonnull ObjectNode returnData) throws IOException {
        sendTextMessage(buildMessage());
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        int optionIdx;
        try {
            optionIdx = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            onInvalidResponse();
            return;
        }
        JsonNode optionsNode = getData().get(KEY_OPTIONS);
        JsonNode optionIdNode = optionsNode.get(optionIdx - 1);
        if (optionIdNode == null) {
            onInvalidResponse();
            return;
        }
        Option item;
        try {
            item = Option.fromId(optionIdNode.asLong());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Invalid option ID {}", optionIdNode.asLong());
            onError();
            return;
        }
        switch (item) {
            case CREATE_LICENSE:
                startPhase(ChatStatus.LICENSE_CREATE);
                break;
            case MANAGE_GROUP:
                startPhase(ChatStatus.GROUP_MANAGE);
                break;
            case DO_NOTHING:
                onDoNothing();
                break;
            default:
                LOGGER.error("Menu item {} isn't handled", item);
                onError();
                break;
        }

    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
        sendTextMessage(buildMessage());
    }

    private void onDoNothing() throws IOException {
        sendTextMessage("好的，有需要再叫我喔~");
        leavePhase();
    }

    private void onError() throws IOException {
        sendTextMessage("糟糕，出問題了，請稍後再試喔...");
        leavePhase();
    }
}
