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
    private final Guard guard;
    private final Account account;
    private final String guestId;
    private final ObjectNode data;
    private ObjectNode newData = null;
    private boolean leaving = false;
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
        leaving = true;
        this.returnData = returnData;
    }

    @Nonnull
    public ObjectNode getData() {
        return data;
    }

    public boolean isLeaving() {
        return leaving;
    }

    @Nullable
    public ObjectNode getReturnData() {
        return returnData;
    }

    @Nullable
    public ChatStatus getNewPhaseStatus() {
        return newPhaseStatus;
    }

    public boolean isCalling() {
        return newPhaseStatus != null;
    }

    @Nullable
    public ObjectNode getNewPhaseData() {
        return newPhaseData;
    }
}
