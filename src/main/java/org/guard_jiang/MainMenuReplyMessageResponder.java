package org.guard_jiang;

import line.thrift.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public class MainMenuReplyMessageResponder extends MessageResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuReplyMessageResponder.class);

    public MainMenuReplyMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata) {
        String text = getMessageText(message);
        if ("1".equals(text)) {
            setNewChatStatus(ChatStatus.REGISTER_GROUP);
            setNewChatMetadata(Collections.emptyMap());
        } else if ("2".equals(text)) {
            setNewChatStatus(ChatStatus.NONE);
            setNewChatMetadata(Collections.emptyMap());
            Message response = new Message();
            response.setText("好的，有需要再敲我喔~");
            addResponse(response);
        } else {
            Message response = new Message();
            response.setText(MainMenuMessageResponder.MESSAGE);
            addResponse(response);
        }
    }
}
