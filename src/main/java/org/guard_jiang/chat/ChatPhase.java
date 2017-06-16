package org.guard_jiang.chat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import line.thrift.Message;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by someone on 4/22/2017.
 */
public abstract class ChatPhase {
    private enum State {
        NONE,
        CALLING,
        LEAVING
    }
    private final Guard guard;
    private final Account account;
    private final String guestId;
    private final ObjectNode data;
    private State state = State.NONE;
    private ObjectNode returnData = null;
    private ChatStatus newPhaseStatus = null;
    private ObjectNode newPhaseData = null;
    public ChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String guestId,
            @Nonnull ObjectNode data) {
        this.guard = guard;
        this.account = account;
        this.guestId = guestId;
        this.data = data;
    }

    public abstract void onEnter() throws IOException;
    public abstract void onReturn(
            @Nonnull ChatStatus returnStatus,
            @Nonnull ObjectNode returnData) throws IOException;
    public abstract void onReceiveTextMessage(@Nonnull String text) throws IOException;

    protected void sendTextMessage(@Nonnull String text) throws IOException {
        getAccount().sendTextMessage(text, getGuestId());
    }

    @Nonnull
    public Guard getGuard() {
        return guard;
    }

    @Nonnull
    public Account getAccount() {
        return account;
    }

    @Nonnull
    public String getGuestId() {
        return guestId;
    }

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

    public void startPhase(
            @Nonnull ChatStatus newPhaseStatus) {
        startPhase(newPhaseStatus, data.objectNode());
    }

    public void leavePhase() {
        leavePhase(getData().objectNode());
    }

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
