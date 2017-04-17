package org.guard_jiang.message;

import line.thrift.ContentType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Chat;
import org.guard_jiang.ChatStatus;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public abstract class MessageResponder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResponder.class);
    protected final Account account;
    protected final Guard guard;
    private boolean chatUpdated;
    private ChatStatus newChatStatus;
    private Map<String, String> newChatMetadata;
    private List<Message> responses = new LinkedList<>();
    private String fromId;

    public MessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
    }

    public void handle(@Nonnull Message message, @Nonnull Chat chat) throws IOException {
        chatUpdated = false;
        fromId = message.getFromId();
        responses.clear();
        ChatStatus chatStatus = chat.getStatus();
        Map<String, String> chatMetadata = chat.getMetadata();

        newChatStatus = chatStatus;
        newChatMetadata = chatMetadata;

        try {
            onReceiveMessage(message, chatMetadata);
        } catch (Exception ex) {
            LOGGER.error("Exception occurs while handling message", ex);
            responses.clear();
            addTextResponse("糟糕，有問題發生了，請稍候再試");
            setNewChatStatus(ChatStatus.NONE);
            setNewChatMetadata(Collections.emptyMap());
        }

        for (Message response : responses) {
            account.sendMessage(response);
        }
        if (chatUpdated) {
            Chat newChat = new Chat(chat.getHostId(), chat.getGuestId(), chat.getChatEnv());
            newChat.setStatus(newChatStatus);
            newChat.setMetadata(newChatMetadata);
            guard.setChat(newChat);
        }
    }

    @Nullable
    protected String getMessageText(@Nonnull Message message) {
         if (!ContentType.NONE.equals(message.getContentType())) {
            return null;
        }
        String text = message.getText();
        if (text == null) {
            LOGGER.warn("Unable to get message text. " +
                    "Make sure you have turn off letter sealing for account {}", account.getMid());
        }
        return text;
    }

    protected void setNewChatStatus(@Nonnull ChatStatus newChatStatus) {
        this.newChatStatus = newChatStatus;
        chatUpdated = true;
    }

    protected void setNewChatMetadata(@Nonnull Map<String, String> newChatMetadata) {
        this.newChatMetadata = newChatMetadata;
        chatUpdated = true;
    }

    protected void addResponse(@Nonnull Message response) {
        response.setToId(fromId);
        responses.add(response);
    }

    protected void addTextResponse(@Nonnull String text) {
        Message message = new Message();
        message.setText(text);
        addResponse(message);
    }

    @Nonnull
    protected abstract void onReceiveMessage(
            @Nonnull Message message,
            @Nonnull Map<String, String> chatMetadata) throws IOException;
}
