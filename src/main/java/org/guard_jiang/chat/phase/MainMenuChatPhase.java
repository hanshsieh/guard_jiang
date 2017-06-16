package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by someone on 4/9/2017.
 */
public class MainMenuChatPhase extends ChatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuChatPhase.class);

    private enum Options {
        CREATE_LICENSE("建立新的金鑰"),
        MANAGE_GROUP("保護或取消保護群組"),
        DO_NOTHING("目前沒有需要幫忙的");
        private final String text;
        Options(@Nonnull String text) {
            this.text = text;
        }

        @Nonnull
        public String getText() {
            return text;
        }
    }

    public MainMenuChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    private static String buildMessage() {
        StringBuilder builder =
                new StringBuilder("您好，請問有什麼需要幫忙的嗎? (請輸入數字)");
        int index = 0;
        for (Options item : Options.values()) {
            builder.append(String.format("\n%d: %s", ++index, item.getText()));
        }
        return builder.toString();
    }

    @Override
    public void onEnter() throws IOException {
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
        int itemIdx;
        try {
            itemIdx = Integer.parseInt(text);
            Options item = Options.values()[itemIdx - 1];
            switch (item) {
                case CREATE_LICENSE:
                    startPhase(ChatStatus.LICENSE_CREATE);
                    break;
                case MANAGE_GROUP:
                    onManageGroup();
                    break;
                case DO_NOTHING:
                    onDoNothing();
                    break;
                default:
                    LOGGER.error("Menu item {} isn't handled", item);
                    sendTextMessage("糟糕，出問題了...");
                    break;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            onInvalidResponse();
        }
    }

    private void onInvalidResponse() throws IOException {
        sendTextMessage("請輸入正確的數字喔!");
    }

    private void onManageGroup() throws IOException {
        startPhase(ChatStatus.GROUP_MANAGE);
    }

    private void onDoNothing() throws IOException {
        sendTextMessage("好的，有需要再叫我喔~");
        leavePhase();
    }
}
