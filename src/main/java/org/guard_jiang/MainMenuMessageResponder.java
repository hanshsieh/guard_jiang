package org.guard_jiang;

import line.thrift.Message;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public class MainMenuMessageResponder extends MessageResponder {
    public static final String MESSAGE =
            "您好，請問有什麼需要幫忙的嗎? (請輸入數字)\n" +
            "1. 幫我保護一個群組\n" +
            "2. 目前沒有需要幫忙的";
    public MainMenuMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata) {
        Message response = new Message();
        response.setText(MESSAGE);
        addResponse(response);
        setNewChatStatus(ChatStatus.MAIN_MENU_REPLY);
        setNewChatMetadata(Collections.emptyMap());
    }
}
