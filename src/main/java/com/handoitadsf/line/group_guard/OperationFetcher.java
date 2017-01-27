package com.handoitadsf.line.group_guard;

import com.handoitadsf.line.group_guard.PrioritizedTask.Listener;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.cslinmiso.line.model.LineClient;
import line.thrift.OpType;
import line.thrift.Operation;

/**
 * Created by cahsieh on 1/27/17.
 */
class OperationFetcher extends PrioritizedTask {

    private static final int NUM_FETCH_OPERATIONS = 50;
    private final GuardRole role;

    public OperationFetcher(@Nonnull GuardRole role, int priority) {
        super(priority);
        this.role = role;
    }

    public void run() {
        try {
            LineClient client = role.getLineClient();
            List<Operation> operations = client.getApi().fetchOperations(
                client.getRevision(), NUM_FETCH_OPERATIONS);
            for (Operation operation : operations) {

                OpType opType = operation.getType();
                switch (opType) {
                    case NOTIFIED_INVITE_INTO_GROUP:
                        // The current user has been invited to join a group.

                    case ACCEPT_GROUP_INVITATION:
                        // The current user has accepted a group invitation
                    case LEAVE_GROUP:
                        // The current user has left a group.
                }

                client.setRevision(Math.max(client.getRevision(), operation.getRevision()));
            }
        } catch (Exception ex) {

        }
    }
}
