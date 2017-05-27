package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import line.thrift.ContentType;
import line.thrift.MIDType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by someone on 4/9/2017.
 */
public class MessageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    private final Account account;
    private final Guard guard;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageManager(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
    }

    public void onReceiveMessage(@Nonnull Message message) throws IOException {
        MIDType midType = message.getToType();
        if (!MIDType.USER.equals(midType)) {
            return;
        }

        ChatEnv chatEnv = new ChatEnv(ChatEnvType.USER, message.getToId());

        ChatManager chatManager = new ChatManager(
                guard, account, message.getFromId(), chatEnv, objectMapper);

        if (ContentType.NONE.equals(message.getContentType()) && message.getText() != null) {
            LOGGER.debug("Receive text message: {}", message.getText());
            chatManager.onReceiveTextMessage(message.getText());
        } else {
            LOGGER.debug("Receive non-text message: {}", message);
        }
    }

}
