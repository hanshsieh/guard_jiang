package org.guard_jiang.chat;

import line.thrift.ContentType;
import line.thrift.MIDType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * A manager for manging the incoming messages.
 * This class should be thread-safe.
 */
@ThreadSafe
public class MessageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    private final ChatManagerFactory chatManagerFactory;

    /**
     * Construct a new instance.
     *
     * @param guard   The guard instance of the system.
     * @param account The LINE account that is receiving the messages.
     */
    public MessageManager(@Nonnull Guard guard, @Nonnull Account account) {
        this.chatManagerFactory = new ChatManagerFactory(guard, account);
    }

    MessageManager(@Nonnull ChatManagerFactory chatManagerFactory) {
        this.chatManagerFactory = chatManagerFactory;
    }

    /**
     * This method should be invoked to handle an incoming message.
     *
     * @param message Message received.
     * @throws IOException IO error occurs.
     */
    public void onReceiveMessage(@Nonnull Message message) throws IOException {
        MIDType midType = message.getToType();

        // Currently, we only handle one-to-one messages.
        if (!MIDType.USER.equals(midType)) {
            return;
        }

        if (!ContentType.NONE.equals(message.getContentType()) ||
                message.getText() == null) {
            LOGGER.debug("Receive non-text message: {}", message);
            return;
        }

        LOGGER.debug("Receive text message: {}", message.getText());
        ChatEnv chatEnv = new ChatEnv(ChatEnvType.USER, message.getToId());
        ChatManager chatManager = chatManagerFactory.createChatManager(
                chatEnv, message.getFromId());
        chatManager.onReceiveTextMessage(message.getText());
    }

}
