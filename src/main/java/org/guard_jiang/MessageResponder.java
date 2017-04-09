package org.guard_jiang;

import line.thrift.ContentType;
import line.thrift.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
    private final Account account;
    private final Guard guard;
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
        String myId = account.getMid();
        ChatStatus chatStatus = chat.getStatus();
        Map<String, String> chatMetadata = chat.getMetadata();

        newChatStatus = chatStatus;
        newChatMetadata = chatMetadata;

        onReceiveMessage(message, chatMetadata);

        for (Message response : responses) {
            account.sendMessage(response);
        }
        if (chatUpdated) {
            Chat newChat = new Chat(myId, message.getFromId());
            newChat.setStatus(newChatStatus);
            newChat.setMetadata(newChatMetadata);
            guard.setChat(newChat);
        }
    }

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

    @Nonnull
    protected abstract void onReceiveMessage(
            @Nonnull Message message,
            @Nonnull Map<String, String> chatMetadata);
}
