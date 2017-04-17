package org.guard_jiang.message;

import line.thrift.MIDType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Chat;
import org.guard_jiang.ChatStatus;
import org.guard_jiang.Guard;
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
    private final Map<ChatStatus, MessageResponder> chatResponders =
            new HashMap<>();

    public MessageManager(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
        MessageResponder mainMenuResponder = new MainMenuMessageResponder(guard, account);
        chatResponders.put(ChatStatus.USER_MAIN_MENU, mainMenuResponder);
        chatResponders.put(ChatStatus.USER_MAIN_MENU_REPLY, new MainMenuReplyMessageResponder(guard, account));
        chatResponders.put(
                ChatStatus.USER_SELECT_LICENSE_FOR_ADD,
                new LicenseSelectMessageResponder(guard, account));
        chatResponders.put(
                ChatStatus.USER_SELECT_GROUP_FOR_LICENSE_ADD,
                new GroupSelectForLicenseMessageResponder(guard, account));
    }

    public void onReceiveMessage(@Nonnull Message message) throws IOException {
        MIDType midType = message.getToType();
        ChatEnv chatEnv;
        ChatStatus defaultStatus = ChatStatus.NONE;
        switch (midType) {
            case USER:
                chatEnv = new ChatEnv(ChatEnvType.USER, message.getToId());
                defaultStatus = ChatStatus.USER_MAIN_MENU;
                break;
            case GROUP:
                chatEnv = new ChatEnv(ChatEnvType.GROUP, message.getToId());
                break;
            case ROOM:
                chatEnv = new ChatEnv(ChatEnvType.ROOM, message.getToId());
                break;
            default:
                return;
        }

        Chat chat = getChat(message, chatEnv, defaultStatus);

        ChatStatus chatStatus = chat.getStatus();
        MessageResponder responder = chatResponders.get(chatStatus);
        if (responder == null) {
            LOGGER.warn("No corresponding responder for chat status {}, envType: {}, envId: {}",
                    chatStatus, chatEnv.getType(), chatEnv.getId());
            return;
        }
        responder.handle(message, chat);
    }

    @Nonnull
    private Chat getChat(@Nonnull Message message,
                         @Nonnull ChatEnv chatEnv,
                         @Nonnull ChatStatus defaultStatus) throws IOException{
        String myId = account.getMid();
        String fromId = message.getFromId();
        Chat chat = guard.getChat(myId, fromId, chatEnv);
        if (chat == null) {
            chat = new Chat(myId, message.getFromId(), chatEnv);
            chat.setStatus(defaultStatus);
        }
        return chat;
    }
}
