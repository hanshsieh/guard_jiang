package org.guard_jiang;

import line.thrift.ContentType;
import line.thrift.MIDType;
import line.thrift.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public class MessageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    private final Account account;
    private final Guard guard;
    private final Map<ChatStatus, MessageResponder> responders =
            new HashMap<>();

    public MessageManager(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
        MessageResponder mainMenuResponder = new MainMenuMessageResponder(guard, account);
        responders.put(ChatStatus.MAIN_MENU, mainMenuResponder);
        responders.put(ChatStatus.NONE, mainMenuResponder);
        responders.put(ChatStatus.MAIN_MENU_REPLY, new MainMenuReplyMessageResponder(guard, account));
    }

    public void onReceiveMessage(@Nonnull Message message) throws IOException {
        MIDType type = message.getToType();
        if (!MIDType.USER.equals(type)) {
            return;
        }
        String myId = account.getMid();
        String fromId = message.getFromId();
        Chat chat = guard.getChat(myId, fromId);
        if (chat == null) {
            chat = new Chat(myId, message.getFromId());
        }
        ChatStatus chatStatus = chat.getStatus();
        MessageResponder responder = responders.get(chatStatus);
        if (responder == null) {
            LOGGER.warn("No corresponding responder for chat status {}", chatStatus);
            return;
        }
        responder.handle(message, chat);
    }
}
