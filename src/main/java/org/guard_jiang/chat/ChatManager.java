package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.ContentType;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.phase.ChatPhase;
import org.guard_jiang.chat.phase.ChatPhaseFactory;
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
    static final int MAX_ROUND_PER_MESSAGE = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatManager.class);

    protected final Account account;
    protected final Guard guard;
    private final ObjectMapper objectMapper;
    private final ChatEnv chatEnv;
    private final String userId;
    private final ChatPhaseFactory chatPhaseFactory;
    private Chat chat;

    public ChatManager(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull ChatEnv chatEnv,
            @Nonnull String userId,
            @Nonnull ObjectMapper objectMapper) {
        this(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                new ChatPhaseFactory(guard, account, userId));
    }

    public ChatManager(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull ChatEnv chatEnv,
            @Nonnull String userId,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull ChatPhaseFactory chatPhaseFactory) {
        this.guard = guard;
        this.account = account;
        this.userId = userId;
        this.chatEnv = chatEnv;
        this.objectMapper = objectMapper;
        this.chatPhaseFactory = chatPhaseFactory;
    }

    private void startChat() throws IOException {
        if(chat == null) {
            chat = buildChat();
        }
    }

    public void onReceiveMessage(@Nonnull Message message) throws IOException {
        if (!ChatEnvType.USER.equals(chatEnv.getType())) {
            return;
        }

        startChat();
        try {
            ChatPhase chatPhase = getChatPhase();
            if (chatPhase == null) {
                Deque<ChatFrame> stack = chat.getStack();
                ChatFrame chatFrame = getDefaultChatFrame();
                stack.addLast(chatFrame);
                chatPhase = chatPhaseFactory.createChatPhase(chatFrame);
                chatPhase.onEnter();
                return;
            }
            chatPhase.onReceiveMessage(message);
            handlePhaseChanges(chatPhase);
        } catch (Exception ex) {
            chat.getStack().clear();
            LOGGER.error("Error occurs when handling message. message: {}", message, ex);
            account.sendTextMessage("糟糕，發生錯誤了...請等會再來找我喔", userId);
        } finally {
            saveChat();
        }
    }

    private void saveChat() throws IOException {
        guard.setChat(chat);
    }

    private void handlePhaseChanges(@Nonnull ChatPhase chatPhase) throws IOException {
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
    }

    @Nullable
    private ChatPhase handleLeavingPhase(@Nonnull ChatPhase chatPhase) throws IOException {
        ChatFrame popedFrame = chat.getStack().removeLast();
        ChatPhase lastChatPhase = getChatPhase();

        if (lastChatPhase != null) {
            ObjectNode returnData = chatPhase.getReturnData();
            assert returnData != null;
            lastChatPhase.onReturn(popedFrame.getChatStatus(), returnData);
        }
        return lastChatPhase;
    }

    @Nonnull
    private ChatPhase handleCallingPhase(@Nonnull ChatPhase chatPhase) throws IOException {
        ChatStatus newStatus = chatPhase.getNewPhaseStatus();
        ObjectNode newStatusData = chatPhase.getNewPhaseData();
        assert newStatus != null;
        assert newStatusData != null;
        ChatFrame newChatFrame = new ChatFrame(newStatus, newStatusData);
        chat.getStack().addLast(newChatFrame);
        chatPhase = chatPhaseFactory.createChatPhase(newChatFrame);
        chatPhase.onEnter();
        return chatPhase;
    }

    @Nonnull
    private Chat buildChat() throws IOException{
        String myId = account.getMid();
        return guard.getChat(myId, userId, chatEnv);
    }

    @Nullable
    private ChatPhase getChatPhase() throws IOException {
        Deque<ChatFrame> stack = chat.getStack();
        if (stack.isEmpty()) {
            return null;
        }
        return chatPhaseFactory.createChatPhase(stack.getLast());
    }

    @Nonnull
    private ChatFrame getDefaultChatFrame() {
        return new ChatFrame(
                ChatStatus.USER_MAIN_MENU,
                objectMapper.createObjectNode()
        );
    }
}
