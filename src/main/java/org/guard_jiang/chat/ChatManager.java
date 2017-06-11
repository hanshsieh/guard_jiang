package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.ContentType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Deque;

/**
 * A manager for a chat between guard account and another LINE account.
 * This class is stateful, and isn't thread-safe.
 */
@NotThreadSafe
public class ChatManager {
    private static final int MAX_ROUND_PER_MESSAGE = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatManager.class);

    protected final Account account;
    protected final Guard guard;
    private final ObjectMapper objectMapper;
    private final ChatEnv chatEnv;
    private final String userId;
    private Chat chat;

    public ChatManager(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull ChatEnv chatEnv, @Nonnull String userId,
            @Nonnull ObjectMapper objectMapper) {
        this.guard = guard;
        this.account = account;
        this.userId = userId;
        this.chatEnv = chatEnv;
        this.objectMapper = objectMapper;
    }

    public void startChat() throws IOException {
        if (chat != null) {
            return;
        }
        chat = getChat();
    }

    public void onReceiveTextMessage(@Nonnull String text) throws IOException {
        startChat();
        try {
            ChatPhase chatPhase = getChatPhase(true);
            if (chatPhase == null) {
                return;
            }
            chatPhase.onReceiveTextMessage(text);
            boolean phaseChanged;
            int round = 0;
            do {
                ++round;
                if (round > MAX_ROUND_PER_MESSAGE) {
                    throw new RuntimeException("Exceeding maximum round per message. round: " + round);
                }
                phaseChanged = false;
                if (chatPhase.isLeaving()) {
                    phaseChanged = true;
                    chatPhase = handleLeavingPhase(chatPhase);
                } else {
                    ObjectNode data = chatPhase.getData();
                    chat.getStack().getLast().setData(data);
                    if (chatPhase.isCalling()) {
                        phaseChanged = true;
                        chatPhase = handleCallingPhase(chatPhase);
                    }
                }
            } while (chatPhase != null && phaseChanged);
        } catch (Exception ex) {
            LOGGER.error("Error occurs when handling message", ex);
            account.sendTextMessage("糟糕，發生錯誤了...請等會再來找我喔", userId);
            chat.getStack().clear();
        }
        saveChat();
    }

    private void saveChat() throws IOException {
        if (chat == null) {
            throw new IllegalStateException("No chat has been started");
        }
        guard.setChat(chat);
    }

    @Nullable
    private ChatPhase handleLeavingPhase(@Nonnull ChatPhase chatPhase) throws IOException {
        ObjectNode returnData = chatPhase.getReturnData();
        ChatFrame popedFrame = chat.getStack().removeLast();
        chatPhase = getChatPhase(false);
        if (chatPhase != null) {
            if (returnData == null) {
                returnData = objectMapper.createObjectNode();
            }
            chatPhase.onReturn(popedFrame.getChatStatus(), returnData);
        }
        return chatPhase;
    }

    @Nullable
    private ChatPhase handleCallingPhase(@Nonnull ChatPhase chatPhase) throws IOException {
        ChatStatus newStatus = chatPhase.getNewPhaseStatus();
        ObjectNode newStatusData = chatPhase.getNewPhaseData();
        assert newStatus != null;
        assert newStatusData != null;
        chat.getStack().addLast(new ChatFrame(newStatus, newStatusData));
        chatPhase = getChatPhase(false);
        if (chatPhase != null) {
            chatPhase.onEnter();
        }
        return chatPhase;
    }

    @Nonnull
    private Chat getChat() throws IOException{
        String myId = account.getMid();
        return guard.getChat(myId, userId, chatEnv);
    }


    @Nullable
    private ChatPhase getChatPhase(boolean createIfNotExist) throws IOException {
        if (chat == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        ChatPhase chatPhase;
        Deque<ChatFrame> stack = chat.getStack();
        if (stack.isEmpty()) {
            if (!createIfNotExist) {
                return null;
            }
            ChatFrame chatFrame = getDefaultChatFrame();
            if (chatFrame == null) {
                return null;
            }
            stack.addLast(chatFrame);
            chatPhase = getChatPhase(chatFrame);
            chatPhase.onEnter();
        } else {
            chatPhase = getChatPhase(stack.getLast());
        }
        return chatPhase;
    }

    @Nonnull
    private ChatPhase getChatPhase(
            @Nonnull ChatFrame chatFrame
    ) throws IOException {

        ChatStatus chatStatus = chatFrame.getChatStatus();
        switch (chatStatus) {
            case USER_MAIN_MENU:
                return new MainMenuChatPhase(
                        guard, account, userId, chatFrame.getData());
            case LICENSE_CREATE:
                return new LicenseCreationChatPhase(
                        guard, account, userId, chatFrame.getData());
            case GROUP_MANAGE:
                return new GroupManageChatPhase(
                        guard, account, userId, chatFrame.getData()
                );
            case ROLE_MANAGE:
                return new RoleManageChatPhase(
                        guard, account, userId, chatFrame.getData()
                );
            case ROLES_ADD:
                return new RolesAddChatPhase(
                        guard, account, userId, chatFrame.getData()
                );
            case LICENSE_SELECT:
                return new LicenseSelectChatPhase(
                        guard, account, userId, chatFrame.getData());
            case GROUP_SELECT:
                return new GroupSelectChatPhase(
                        guard, account, userId, chatFrame.getData());
            default:
                throw new IllegalArgumentException(
                        "Unsupported chat status " + chatStatus);
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

    @Nullable
    private ChatFrame getDefaultChatFrame() {
        if (!ChatEnvType.USER.equals(chatEnv.getType())) {
            return null;
        }
        return new ChatFrame(
                ChatStatus.USER_MAIN_MENU,
                objectMapper.createObjectNode());
    }
}
