package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This class represents a phase in during a chat.
 * A chat is simulated as a stack of chat frames.
 * A chat frame is similar to the stack frame of a program.
 * A {@link ChatPhase} instance can be instantiated from a {@link org.guard_jiang.chat.ChatFrame}.
 * You may imagine a {@link ChatPhase} as the corresponding function of a stack frame.
 * A {@link ChatPhase} can invoke another {@link ChatPhase}, and the
 * {@link org.guard_jiang.chat.ChatFrame} of the new chat phase will be pushed into the stack.
 * When a {@link ChatPhase} returns, its {@link org.guard_jiang.chat.ChatFrame} is popped from the stack,
 * and data can be returned to the calling {@link ChatPhase}.
 */
public abstract class ChatPhase {
    private enum State {
        NONE,
        CALLING,
        LEAVING
    }
    private final Guard guard;
    private final Account account;
    private final String userId;
    private final ObjectNode data;
    private State state = State.NONE;
    private ObjectNode returnData = null;
    private ChatStatus newPhaseStatus = null;
    private ObjectNode newPhaseData = null;

    /**
     * Construct a new instance.
     *
     * @param guard   The {@link Guard} instance of the system.
     * @param account The guard account handling the chat.
     * @param userId The ID of the LINE user that the guard account is chatting with.
     * @param data The {@link org.guard_jiang.chat.ChatFrame} data.
     */
    public ChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String userId,
            @Nonnull ObjectNode data) {
        this.guard = guard;
        this.account = account;
        this.userId = userId;
        this.data = data;
    }

    /**
     * It should be invoked when the chat phase is first entered.
     *
     * @throws IOException IO error occurs.
     */
    public abstract void onEnter() throws IOException;

    /**
     * It should be invoked when the chat returns from another {@link ChatPhase} to this
     * {@link ChatPhase}.
     *
     * @param returnStatus The {@ChatStatus} of the returning {@link ChatPhase}.
     * @param returnData   The data returned from the returning {@link ChatPhase}.
     * @throws IOException IO error occurs.
     */
    public abstract void onReturn(
            @Nonnull ChatStatus returnStatus,
            @Nonnull ObjectNode returnData) throws IOException;

    /**
     * It should be invoked when a text message is received from the LINE user.
     *
     * @param text Message text.
     * @throws IOException IO error occurs.
     */
    public abstract void onReceiveTextMessage(@Nonnull String text) throws IOException;

    /**
     * Send text message to the LINE user.
     *
     * @param text Message text.
     * @throws IOException IO error occurs.
     */
    protected void sendTextMessage(@Nonnull String text) throws IOException {
        getAccount().sendTextMessage(text, getUserId());
    }

    /**
     * Get the {@link Guard} instance.
     *
     * @return {@link Guard} instance.
     */
    @Nonnull
    public Guard getGuard() {
        return guard;
    }

    /**
     * Get the guard account.
     *
     * @return Guard account.
     */
    @Nonnull
    public Account getAccount() {
        return account;
    }

    /**
     * Get the LINE user ID the guard account is chatting with.
     *
     * @return LINE user ID.
     */
    @Nonnull
    public String getUserId() {
        return userId;
    }

    /**
     * Record that another {@link ChatPhase} should be pushed to the stack.
     * Only one of this method and {@link ChatPhase#leavePhase(ObjectNode)} can only be invoked, and
     * only once; otherwise, {@link IllegalStateException} will be thrown.
     *
     * @param newPhaseStatus The {@link ChatStatus} of the new {@link ChatPhase}.
     * @param newPhaseData The data to passed to the new {@link ChatPhase}.
     */
    public void startPhase(
            @Nonnull ChatStatus newPhaseStatus,
            @Nonnull ObjectNode newPhaseData) {
        if (!State.NONE.equals(state)) {
            throw new IllegalStateException(
                    "This phase is already going to leave or "
                    + "entering another phase");
        }
        this.state = State.CALLING;
        this.newPhaseStatus = newPhaseStatus;
        this.newPhaseData = newPhaseData;
    }

    /**
     * This method has the same effect as invoking {@link ChatPhase#startPhase(ChatStatus, ObjectNode)}
     * with empty data.
     *
     * @param newPhaseStatus The {@link ChatStatus} of the new {@link ChatPhase}.
     */
    public void startPhase(
            @Nonnull ChatStatus newPhaseStatus) {
        startPhase(newPhaseStatus, data.objectNode());
    }

    /**
     * This method has the same effect as invoking {@link ChatPhase#leavePhase(ObjectNode)}
     * with empty return data.
     *
     */
    public void leavePhase() {
        leavePhase(data.objectNode());
    }

    /**
     * Record that this {@link ChatPhase} should leave, and return to the last {@link ChatPhase} on the stack.
     * Only one of this method and {@link ChatPhase#startPhase(ChatStatus, ObjectNode)} can only be invoked, and
     * only once; otherwise, {@link IllegalStateException} will be thrown.
     *
     * @param returnData The data to be returned to the caller.
     */
    public void leavePhase(@Nonnull ObjectNode returnData) {
        if (!State.NONE.equals(state)) {
            throw new IllegalStateException(
                    "This phase is already going to leave or "
                            + "entering another phase");
        }
        this.state = State.LEAVING;
        this.returnData = returnData;
    }

    /**
     * Get the data of this phase.
     *
     * @return Data.
     */
    @Nonnull
    public ObjectNode getData() {
        return data;
    }

    /**
     * Whether this chat phase is leaving.
     *
     * @return Leaving or not.
     */
    public boolean isLeaving() {
        return State.LEAVING.equals(state);
    }

    /**
     * Get the data to be returned from the leaving phase.
     * If this chat phase isn't leaving, null is returned.
     *
     * @return Returned data if this phase is leaving; otherwise, null.
     */
    @Nullable
    public ObjectNode getReturnData() {
        return returnData;
    }

    /**
     * Get the new phase status if this phase is going to enter another phase.
     * If this chat phase isn't going to enter another phase, null is returned.
     *
     * @return New chat status or null.
     */
    @Nullable
    public ChatStatus getNewPhaseStatus() {
        return newPhaseStatus;
    }

    /**
     * Get new chat phase's data if this phase is calling another phase.
     * Otherwise, null is returned.
     *
     * @return New phase data or null.
     */
    @Nullable
    public ObjectNode getNewPhaseData() {
        return newPhaseData;
    }

    /**
     * Whether this phase is going to enter another phase.
     *
     * @return True if it is calling another phose.
     */
    public boolean isCalling() {
        return State.CALLING.equals(state);
    }
}
