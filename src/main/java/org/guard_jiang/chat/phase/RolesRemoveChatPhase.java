package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by someone on 4/24/2017.
 */
public class RolesRemoveChatPhase extends ChatPhase {

    public static final String ARG_ROLE = "role";

    public RolesRemoveChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        super(guard, account, guestId, data);
    }

    @Override
    public void onEnter() throws IOException {
        // TODO
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        // TODO
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        // TODO
    }
}
