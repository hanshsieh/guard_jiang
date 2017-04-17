package org.guard_jiang.message;

import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.ChatStatus;
import org.guard_jiang.Guard;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 4/9/2017.
 */
public class MainMenuMessageResponder extends MessageResponder {
    public MainMenuMessageResponder(@Nonnull Guard guard, @Nonnull Account account) {
        super(guard, account);
    }

    @Nonnull
    @Override
    protected void onReceiveMessage(@Nonnull Message message, @Nonnull Map<String, String> metadata) {
        Message response = new Message();
        response.setText(buildMessage());
        addResponse(response);
        setNewChatStatus(ChatStatus.USER_MAIN_MENU_REPLY);
        setNewChatMetadata(Collections.emptyMap());
    }

    public static String buildMessage() {
        StringBuilder builder =
                new StringBuilder("您好，請問有什麼需要幫忙的嗎? (請輸入數字)");
        int index = 0;
        for (MainMenuItem item : MainMenuItem.values()) {
            builder.append(String.format("\n%d: %s", ++index, item.getText()));
        }
        return builder.toString();
    }
}
